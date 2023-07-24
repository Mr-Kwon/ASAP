package com.d103.asaf.ui.home.pro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.d103.asaf.R
import com.d103.asaf.common.component.CustomToggleButton
import com.d103.asaf.common.config.BaseFragment
import com.d103.asaf.databinding.FragmentProHomeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProHomeFragment : BaseFragment<FragmentProHomeBinding>(FragmentProHomeBinding::bind, R.layout.fragment_pro_home) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var toggleButton : CustomToggleButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleButton = CustomToggleButton(requireContext())
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProHomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}