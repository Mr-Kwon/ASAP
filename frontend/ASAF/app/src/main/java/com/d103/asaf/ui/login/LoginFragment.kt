package com.d103.asaf.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.d103.asaf.R
import com.d103.asaf.databinding.FragmentLoginBinding
import androidx.navigation.fragment.findNavController
import com.d103.asaf.MainActivity
import com.d103.asaf.SharedViewModel
import com.d103.asaf.common.config.ApplicationClass

import com.d103.asaf.common.config.BaseFragment
import com.d103.asaf.databinding.FragmentScheduleBinding


import com.d103.asaf.common.model.dto.Member
import com.d103.asaf.ui.home.student.StudentHomeFragment

import com.google.android.material.bottomnavigation.BottomNavigationView
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "LoginFragment_cjw"
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {
    private var param1: String? = null
    private var param2: String? = null
    private val viewModel: LoginFragmentViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()

//        (requireActivity() as MainActivity).hideBottomNavigationBarFromFragment()

        //        sharedPreference에서 있으면 바로 화면 넘어가기
        if (ApplicationClass.sharedPreferences.getString("memberEmail")?.isNotEmpty() == true) {
            if (ApplicationClass.sharedPreferences.getString("authority") == "stu") {
                findNavController().navigate(R.id.navigation_student_home)
            } else {
                findNavController().navigate(R.id.action_loginFragment_to_ProhomeFragment)
            }
        }
    }

    private fun setupViews() {
        binding.fragmentLoginButtonLogin.setOnClickListener {
            val email = binding.fragmentLoginEditTvId.text.toString()
            val password = binding.fragmentLoginEditTvPass.text.toString()
            viewModel.login(email, password)
        }

        binding.fragmentLoginTextviewForgetpass.setOnClickListener {
            findNavController().navigate(R.id.findpwd_fragment)
        }

        binding.fragmentLoginButtonJoin.setOnClickListener {
            findNavController().navigate(R.id.join_fragment)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner, Observer { loginResult ->
            if (loginResult != Member()) {
                Toast.makeText(
                    context,
                    "${loginResult.memberName}님, 로그인 되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                if(binding.fragmentLoginSwitchAutologin.isChecked){
                    ApplicationClass.sharedPreferences.addUserByEmailAndPwd(loginResult)
                }
//                ApplicationClass.sharedPreferences.addUserByEmailAndPwd(loginResult)
                sharedViewModel.logInUser = loginResult
                Log.d(TAG, "observeViewModel______: ${sharedViewModel.logInUser.memberEmail}")
                sharedViewModel.getClassInfo(loginResult)
                if (loginResult.authority == "stu") {
                    findNavController().navigate(R.id.navigation_student_home)
                } else {
                    findNavController().navigate(R.id.action_loginFragment_to_ProhomeFragment)
                }

            } else {
                Toast.makeText(context, "ID 혹은 패스워드를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
