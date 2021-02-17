package com.mymasimo.masimosleep.ui.session.no_data_yet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mymasimo.masimosleep.R


class SessionNoDataFragment : Fragment() {

    companion object {
        fun newInstance() : SessionNoDataFragment {
            return SessionNoDataFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_session_no_data, container, false)
    }


}