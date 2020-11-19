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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import yanzm.sample.notepad.R
import yanzm.sample.notepad.data.Note
import yanzm.sample.notepad.databinding.FragmentListBinding

class ListFragment : Fragment() {

    interface NoteListFragmentListener {
        fun onAddNote()
        fun onNoteSelected(note: Note)
    }

    private var listener: NoteListFragmentListener? = null

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListViewModel by viewModels()

    private val adapter = NoteListAdapter {
        listener?.onNoteSelected(it)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NoteListFragmentListener
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        adapter.selectionEnabled = resources.getBoolean(R.bool.isChoiceMode)

        if (savedInstanceState != null) {
            adapter.selectedNoteId = savedInstanceState.getLong("selectedNoteId")
        }

        viewModel.list.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
            }

            adapter.submitList(list)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("selectedNoteId", adapter.selectedNoteId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add -> {
                listener?.onAddNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onNoteSelected(noteId: Long) {
        adapter.selectedNoteId = noteId
    }
}
