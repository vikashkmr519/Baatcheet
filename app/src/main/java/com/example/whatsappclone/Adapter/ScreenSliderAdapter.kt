package com.example.whatsappclone.Adapter

import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsappclone.Fragment.InboxFragment
import com.example.whatsappclone.Fragment.PeopleFragment
import com.example.whatsappclone.MainActivity

class ScreenSliderAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when(position){
        0 -> InboxFragment()
        else -> PeopleFragment()
    }

}