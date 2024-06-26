package com.ilsa1000ri.weatherSecretary.ui.friends

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.ilsa1000ri.weatherSecretary.R
import com.ilsa1000ri.weatherSecretary.ui.friends.Friend
import com.ilsa1000ri.weatherSecretary.ui.friends.FriendsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import android.text.Editable
import android.text.TextWatcher
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

class FriendsFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FriendsAdapter
    private val currentUser = auth.currentUser
    private val friendsList = mutableListOf<Friend>()
    private var searchText: String = ""
    private lateinit var noFriendsMessage: TextView  // 추가된 메시지 TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        // RecyclerView의 ID는 XML 파일과 일치해야 함
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.friendsList)
        noFriendsMessage = view.findViewById(R.id.noFriendsMessage)  // TextView 초기화
        recyclerView.layoutManager = LinearLayoutManager(context)
        // recyclerView가 초기화된 후에 설정 메서드 호출
        setupRecyclerView()
        view.findViewById<Button>(R.id.plusFriend).setOnClickListener {
            // 초대 링크 생성 및 공유
            createAndShareLink()
        }
        // 예시: 데이터 로딩
        loadFriendsData()
        setupSearchBar(view)
    }

    override fun onStart() {
        super.onStart()
        handleDynamicLink()
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(mutableListOf(), { friend ->
            navigateToFriendTimetable(friend)
        }, { friend ->
            showDeleteConfirmationDialog(friend)
        })
        recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(friend: Friend) {
        AlertDialog.Builder(context)
            .setTitle("친구 끊기")
            .setMessage("선택한 친구를 삭제합니다")
            .setPositiveButton("네") { dialog, which ->
                deleteFriend(friend)
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun deleteFriend(friend: Friend) {
        // Update UI
        adapter.deleteFriend(friend)

        // Delete from database logic here
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val friendUserRef = db.collection(currentUserUid).document("Friend")
        db.runTransaction { transaction ->
            transaction.update(friendUserRef, mapOf(friend.name to FieldValue.delete()))
            null
        }.addOnSuccessListener {
            Log.d("Firestore", "Friend deleted successfully")
        }
    }

    private fun navigateToFriendTimetable(friend: Friend) {
        val action = FriendsFragmentDirections.actionShowFriendTimetable(friend.name)
        findNavController().navigate(action)
    }

    private fun createAndShareLink() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // 현재 사용자 UID를 안전하게 가져옴
        val invitationLink = "https://weathersecretary.page.link/invite?invitedby=$userId"

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(invitationLink))
            .setDomainUriPrefix("https://weathersecretary.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder().build()
            ) // 필요한 경우, AndroidParameters 설정
            .buildShortDynamicLink()
            .addOnSuccessListener { result ->
                val shortLink = result.shortLink
                sendInviteLink(shortLink.toString())
            }
            .addOnFailureListener { e ->
                Log.e("DynamicLink", "Error creating dynamic link", e)
                Toast.makeText(requireContext(), "Link creation failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendInviteLink(inviteLink: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on Weather Secretary: $inviteLink")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Link"))
    }

    private fun handleDynamicLink() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(activity?.intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                val invitedByUid = deepLink?.getQueryParameter("invitedby")
                if (invitedByUid != null) {
                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserUid == null) {
                        Log.e("DynamicLink", "No current user logged in")
                        return@addOnSuccessListener
                    }
                    addNewFriend(invitedByUid, currentUserUid)
                    Log.d("DynamicLink", "Deep link found: $deepLink")
                    Log.d("DynamicLink", "Invited by UID: $invitedByUid")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DynamicContact", "Handling Dynamic Link failed", e)
            }
    }

    private fun addNewFriend(invitedByUid: String, currentUserUid: String) {
        if (invitedByUid == currentUserUid) {
            Log.d("Firebase", "Cannot add oneself as friend")
            return
        }

        val currentUserRef = db.collection(currentUserUid).document("UserInfo")
        val invitedByUserRef = db.collection(invitedByUid).document("UserInfo")

        currentUserRef.get().addOnSuccessListener { currentUserDoc ->
            if (currentUserDoc.exists()) {
                val currentUserData = currentUserDoc.data
                val currentUserName = currentUserData?.get("name") as? String ?: "Unknown"

                invitedByUserRef.get().addOnSuccessListener { invitedByUserDoc ->
                    if (invitedByUserDoc.exists()) {
                        val invitedByUserData = invitedByUserDoc.data
                        val invitedByUserName = invitedByUserData?.get("name") as? String ?: "Unknown"

                        // Firestore에 친구 관계 추가
                        val userFriendRef = db.collection(currentUserUid).document("Friend")
                        val friendUserRef = db.collection(invitedByUid).document("Friend")

                        // Check if Friend document exists, if not create it
                        // Check if Friend document exists, if not create it
                        userFriendRef.get().addOnSuccessListener { userFriendDoc ->
                            if (!userFriendDoc.exists()) {
                                userFriendRef.set(emptyMap<String, Any>())  // Create an empty Friend document
                            }

                            friendUserRef.get().addOnSuccessListener { friendUserDoc ->
                                if (!friendUserDoc.exists()) {
                                    friendUserRef.set(emptyMap<String, Any>())  // Create an empty Friend document
                                }


                                // Now add friends
                                db.runTransaction { transaction ->
                                    // currentUser의 Friends에 초대 사용자의 정보를 맵으로 저장
                                    transaction.update(userFriendRef, mapOf(invitedByUserName to mapOf("uid" to invitedByUid, "isFavorite" to false)))
                                    // 초대 사용자의 Friends에 currentUser의 정보를 맵으로 저장
                                    transaction.update(friendUserRef, mapOf(currentUserName to mapOf("uid" to currentUserUid, "isFavorite" to false)))
                                    null
                                }.addOnSuccessListener {
                                    Log.d("Firestore", "Friend relation added successfully between $currentUserUid and $invitedByUid")
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Failed to add friend relation", e)
                                }
                            }.addOnFailureListener { e ->
                                Log.e("Firestore", "Failed to check or create Friend document for invited user", e)
                            }
                        }.addOnFailureListener { e ->
                            Log.e("Firestore", "Failed to check or create Friend document for current user", e)
                        }
                    } else {
                        Log.d("FirestoreData", "No such document for invited user")
                    }
                }.addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to get invited by user name", e)
                }
            } else {
                Log.d("FirestoreData", "No such document for current user")
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Failed to get current user name", e)
        }
    }



    private fun setupSearchBar(view: View) {
        val searchBar = view.findViewById<EditText>(R.id.searchbar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 사용자가 입력한 값을 필터링합니다.
                searchText = s.toString()
                filterFriends(searchText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFriends(searchText: String) {
        val filteredFriends = friendsList.filter { friend ->
            friend.name.contains(searchText, ignoreCase = true)
        }
        adapter.updateData(filteredFriends)
    }

    private fun loadFriendsData() {
        currentUser?.let { user ->
            val userId = user.uid
            // 특정 문서("Friend")를 가져옴
            db.collection(userId).document("Friend")
                .get()
                .addOnSuccessListener { document ->
                    friendsList.clear()
                    if (document.exists()) {
                        // 문서가 존재하면 데이터를 가져와서 처리
                        document.data?.forEach { (name, data) ->
                            if (data is Map<*, *>) {
                                val uid = data["uid"] as? String
                                val isFavorite = data["isFavorite"] as? Boolean ?: false
                                friendsList.add(Friend(name, uid, isFavorite))
                            }
                        }
                        if (friendsList.isNotEmpty()) {
                            friendsList.sortByDescending { it.isFavorite }
                            adapter.updateData(friendsList)
                            noFriendsMessage.visibility = View.GONE  // 친구가 있으면 메시지 숨김
                        } else {
                            Log.d("FirestoreData", "No friends found")
                            noFriendsMessage.visibility = View.VISIBLE  // 친구가 없으면 메시지 표시
                        }
                    } else {
                        Log.d("FirestoreData", "No Friend document found")
                        noFriendsMessage.visibility = View.VISIBLE  // 문서가 없으면 메시지 표시
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching Friend document", e)
                    noFriendsMessage.visibility = View.VISIBLE  // 오류 시 메시지 표시
                }
        }
    }
}
