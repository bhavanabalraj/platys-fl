package com.example.platys.register

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.platys.R
import com.example.platys.data.EventObserver
import com.example.platys.databinding.RegisterFragmentBinding
import com.example.platys.navigation.NavigationDestination
import com.google.android.material.progressindicator.ProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class RegisterFragment: DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<RegisterViewModel> { viewModelFactory }

    private lateinit var btRegister: MaterialTextView

    private lateinit var motionLayout: MotionLayout

    private lateinit var registerIndicator: ProgressIndicator

    private lateinit var emailTextInputLayout: TextInputLayout

    private lateinit var passwordTextInputLayout: TextInputLayout

    private lateinit var viewDataBinding: RegisterFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.register_fragment, container, false)

        viewDataBinding = RegisterFragmentBinding.bind(root).apply {
            this.viewmodel = viewModel
        }

        motionLayout = root.findViewById(R.id.register_motion_scene)

        // Hack to start to login screen animation. Bug in motion layout. Auto transition not working
        val imageView = root.findViewById<ImageView>(R.id.registerImageView)
        btRegister = root.findViewById(R.id.btRegister)
        registerIndicator = root.findViewById(R.id.registerIndicator)
        emailTextInputLayout = root.findViewById(R.id.tlEmail)
        passwordTextInputLayout = root.findViewById(R.id.tlPassword)

        Handler().postDelayed({
            imageView.performClick()
        }, 100)

        registerIndicator.bringToFront()
        return root
    }

    private fun setUpObservers() {
        viewModel.dataLoaded.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                true -> {
                    btRegister.text = ""
                    motionLayout.setTransition(R.id.registerBeginStart, R.id.registerBeginEnd)
                    motionLayout.transitionToEnd()
                }
                false -> {
                    motionLayout.setTransition(R.id.registerOverStart, R.id.registerOverEnd)
                    motionLayout.transitionToEnd()
                    btRegister.text = resources.getString(R.string.register)
                }
            }
        })

        viewModel.clearEmailError.observe(viewLifecycleOwner, EventObserver {
            emailTextInputLayout.error = null
        })

        viewModel.clearPasswordError.observe(viewLifecycleOwner, EventObserver {
            passwordTextInputLayout.error = null
        })

        viewModel.fieldErrors.observe(viewLifecycleOwner, EventObserver { errorMessage ->
            motionLayout.setTransition(R.id.fieldErrorAnimStart, R.id.fieldErrorAnimEnd)
            motionLayout.transitionToEnd()
            val emailError = errorMessage.split("$")[0]
            val passwordError = errorMessage.split("$")[1]
            emailTextInputLayout.error = emailError
            passwordTextInputLayout.error = passwordError
        })

        viewModel.emailError.observe(viewLifecycleOwner, EventObserver {errorMessage ->
            motionLayout.setTransition(R.id.emailErrorAnimStart, R.id.emailErrorAnimEnd)
            motionLayout.transitionToEnd()
            emailTextInputLayout.error = errorMessage
        })

        viewModel.passwordError.observe(viewLifecycleOwner, EventObserver {errorMessage ->
            motionLayout.setTransition(R.id.passwordErrorAnimStart, R.id.passwordErrorAnimEnd)
            motionLayout.transitionToEnd()
            passwordTextInputLayout.error = errorMessage
        })

        viewModel.navigationEvent.observe(viewLifecycleOwner, EventObserver { destination ->
            when(destination) {
                NavigationDestination.REGISTER_SUCCESS -> {
                    val action = RegisterFragmentDirections.actionRegisterFragmentToSuccessFragment()
                    findNavController().navigate(action)
                }
                NavigationDestination.REGISTER_FAILURE -> {
                    motionLayout.setTransition(R.id.fieldErrorAnimStart, R.id.fieldErrorAnimEnd)
                    motionLayout.transitionToEnd()
                    Toast.makeText(activity, "User registration failed. Please check your network connection.", Toast.LENGTH_LONG).show()
                }
                NavigationDestination.REGISTER_TO_LOGIN -> {
                    activity?.onBackPressed()
                }
                else -> {}
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setUpObservers()
        setUpTextWatchers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onEmailChanged(emailTextInputLayout.editText?.text.toString())
        viewModel.onPasswordChanged(passwordTextInputLayout.editText?.text.toString())
        viewModel.onResume()
    }

    private fun setUpTextWatchers() {
        emailTextInputLayout.editText?.doOnTextChanged { input, _, _, _  ->
            viewModel.onEmailChanged(input.toString())
        }

        passwordTextInputLayout.editText?.doOnTextChanged { input, _, _, _  ->
            viewModel.onPasswordChanged(input.toString())
        }
    }
}