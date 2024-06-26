package com.ilsa1000ri.weatherSecretary.ui.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ilsa1000ri.weatherSecretary.R

class FriendsAdapter(
    private var friends: MutableList<Friend>,
    private val onItemClick: (Friend) -> Unit,
    private val onDeleteClick: (Friend) -> Unit,


) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position], onItemClick, onDeleteClick) { favoritedFriend ->
            // Firestore에 즐겨찾기 상태 업데이트
            updateFavoriteStatus(favoritedFriend) { success ->
                if (success) {
                    sortFriendsAndUpdateAdapter() // 이 부분을 추가하여 빠른 UI 반영

                }
            }
        }
    }

    override fun getItemCount(): Int = friends.size

    fun updateData(newFriends: List<Friend>) {
        this.friends.clear()
        this.friends.addAll(newFriends)
        notifyDataSetChanged()
    }

    fun deleteFriend(friend: Friend) {
        val position = friends.indexOf(friend)
        if (position >= 0) {
            friends.removeAt(position)
            notifyItemRemoved(position)
        }
    }



    private fun updateFavoriteStatus(friend: Friend, callback: (Boolean) -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val friendUserRef = FirebaseFirestore.getInstance().collection(currentUserUid).document("Friend")

        friend.isFavorite = !friend.isFavorite // 상태 토글

        val updateData = mapOf(
            friend.name to mapOf(
                "uid" to friend.uid,
                "isFavorite" to friend.isFavorite
            )
        )

        friendUserRef.update(updateData).addOnSuccessListener {
            Log.d("Firestore", "Favorite status updated successfully")
            sortFriendsAndUpdateAdapter() // 전체 리스트를 새롭게 정렬하고 어댑터를 업데이트
            callback(true)
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Failed to update favorite status", e)
            callback(false)
        }
    }

    private fun sortFriendsAndUpdateAdapter() {
        friends.sortWith(compareByDescending<Friend> { it.isFavorite }.thenBy { it.name })
        notifyDataSetChanged()
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.friendName)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton) // 추가: 삭제 버튼
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)

        fun bind(friend: Friend, onItemClick: (Friend) -> Unit, onDeleteClick: (Friend) -> Unit, onFavoriteClick: (Friend) -> Unit) {
            nameTextView.text = friend.name
            favoriteButton.isSelected = friend.isFavorite

            itemView.setOnClickListener { onItemClick(friend) }
            deleteButton.setOnClickListener { onDeleteClick(friend) }

            favoriteButton.setOnClickListener {
                onFavoriteClick(friend)
                favoriteButton.isSelected = !favoriteButton.isSelected // 즉시 토글 시각적 피드백
            }
        }
    }
}
