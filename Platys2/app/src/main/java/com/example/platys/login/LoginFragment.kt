package com.example.platys.login

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
import com.example.platys.data.Event
import com.example.platys.data.EventObserver
import com.example.platys.databinding.LoginFragmentBinding
import com.example.platys.databinding.RegisterFragmentBinding
import com.example.platys.databinding.SplashFragmentBinding
import com.example.platys.navigation.NavigationDestination
import com.google.android.material.progressindicator.ProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class LoginFragment: DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }

    private lateinit var btLogin: MaterialTextView

    private lateinit var tvRegister: MaterialTextView

    private lateinit var motionLayout: MotionLayout

    private lateinit var loginIndicator: ProgressIndicator

    private lateinit var emailTextInputLayout: TextInputLayout

    private lateinit var passwordTextInputLayout: TextInputLayout

    private lateinit var viewDataBinding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.login_fragment, container, false)

        viewDataBinding = LoginFragmentBinding.bind(root).apply {
            this.viewmodel = viewModel
        }

        motionLayout = root.findViewById(R.id.login_motion_scene)

        // Hack to start to login screen animation. Bug in motion layout. Auto transition not working
        val imageView = root.findViewById<ImageView>(R.id.loginImageView)
        Handler().postDelayed({
            imageView.performClick()
        }, 100)

        btLogin = root.findViewById(R.id.btLogin)
        tvRegister = root.findViewById(R.id.tvRegisterLink)
        loginIndicator = root.findViewById(R.id.loginIndicator)
        emailTextInputLayout = root.findViewById(R.id.tlEmail)
        passwordTextInputLayout = root.findViewById(R.id.tlPassword)

        loginIndicator.bringToFront()
        return root
    }

    private fun setUpObservers() {
        viewModel.dataLoaded.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                true -> {
                    btLogin.text = ""
                    tvRegister.visibility = View.INVISIBLE
                    motionLayout.setTransition(R.id.loginStart, R.id.loginEnd)
                    motionLayout.transitionToEnd()
                }
                false -> {
                    motionLayout.setTransition(R.id.loginButtonClickDoneStart, R.id.loginButtonClickDoneEnd)
                    motionLayout.transitionToEnd()
                    btLogin.text = resources.getString(R.string.login)
                    tvRegister.visibility = View.VISIBLE
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setUpNavigation()
        setUpObservers()
        setUpTextWatchers()
    }

    private fun setUpNavigation() {
        viewModel.navigationEvent.observe(viewLifecycleOwner, EventObserver { destination ->
            when (destination) {
                NavigationDestination.LOGIN_TO_REGISTER -> {
                    val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                    findNavController().navigate(action)
                }
                NavigationDestination.LOGIN_SUCCESS -> {
                    //Toast.makeText(activity, "Login successful", Toast.LENGTH_LONG).show()
                    val action = LoginFragmentDirections.actionLoginFragmentToContextTagFragment()
                    findNavController().navigate(action)
                }
                NavigationDestination.LOGIN_FAILURE -> {
                    motionLayout.setTransition(R.id.fieldErrorAnimStart, R.id.fieldErrorAnimEnd)
                    motionLayout.transitionToEnd()
                    Toast.makeText(activity, "Authentication failed. Please check your credentials & network connection.", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        })
    }

    private fun setUpTextWatchers() {
        emailTextInputLayout.editText?.doOnTextChanged { input, _, _, _  ->
            viewModel.onEmailChanged(input.toString())
        }

        passwordTextInputLayout.editText?.doOnTextChanged { input, _, _, _  ->
            viewModel.onPasswordChanged(input.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onEmailChanged(emailTextInputLayout.editText?.text.toString())
        viewModel.onPasswordChanged(passwordTextInputLayout.editText?.text.toString())
        viewModel.onResume()
    }
}