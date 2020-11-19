/*
 * Copyright (C) 2011 Yuki Anzai, uPhyca Inc.
 *      http://www.uphyca.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yanzm.sample.notepad.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import yanzm.sample.notepad.data.Note
import yanzm.sample.notepad.databinding.ListItemBinding

class NoteListAdapter(private val clickListener: (Note) -> Unit) :
    ListAdapter<Note, NoteViewHolder>(NoteDiffCallback()) {

    var selectionEnabled = false

    var selectedNoteId: Long = 0L
        set(value) {
            val lastValue = field
            field = value
            if (selectionEnabled && value != lastValue) {
                if (lastValue != 0L) {
                    val index = (0 until itemCount).indexOfFirst {
                        getItem(it).id == lastValue
                    }
                    if (index >= 0) {
                        notifyItemChanged(index)
                    }
                }
                if (value != 0L) {
                    val index = (0 until itemCount).indexOfFirst {
                        getItem(it).id == value
                    }
                    if (index >= 0) {
                        notifyItemChanged(index)
                    }
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder.create(parent).apply {
            itemView.setOnClickListener {
                val note = getItem(adapterPosition)
                clickListener(note)
            }
        }
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, selectedNoteId == note.id)
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}

class NoteViewHolder private constructor(private val binding: ListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note, isSelected: Boolean) {
        binding.titleView.text = note.title
        binding.bodyView.text = note.body
        binding.check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
    }

    companion object {
        fun create(parent: ViewGroup): NoteViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return NoteViewHolder(ListItemBinding.inflate(inflater, parent, false))
        }
    }
}
