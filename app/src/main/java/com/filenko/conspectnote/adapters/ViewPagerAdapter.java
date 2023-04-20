package com.filenko.conspectnote.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.filenko.conspectnote.activity.cards.PageFragment;
import com.filenko.conspectnote.model.Note;

import java.util.List;


public class ViewPagerAdapter extends FragmentStateAdapter {
    private final List<Note> objects;
    public ViewPagerAdapter(FragmentActivity fragmentActivity, List<Note> objects) {
        super(fragmentActivity);
        this.objects = objects;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new PageFragment(this.objects.get(position), position, objects.size());
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }
}
