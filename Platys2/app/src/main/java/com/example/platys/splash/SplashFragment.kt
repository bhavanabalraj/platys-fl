package com.example.platys.splash

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.platys.data.EventObserver
import com.example.platys.databinding.SplashFragmentBinding
import com.example.platys.navigation.NavigationDestination
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SplashFragment: DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<SplashViewModel> { viewModelFactory }

    private lateinit var viewDataBinding: SplashFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = SplashFragmentBinding.inflate(inflater, container, false).apply {
            this.viewmodel = viewModel
        }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setUpNavigation()
        Handler().postDelayed({
            viewModel.start()
        }, 1000)
    }

    private fun setUpNavigation() {
        viewModel.navigationEvent.observe(viewLifecycleOwner, EventObserver {destination ->
            when(destination) {
                NavigationDestination.SPLASH_TO_LOGIN -> {
                    val action = SplashFragmentDirections.actionSplashFragmentToLoginFragment()
                    findNavController().navigate(action)
                }
                else -> {}
            }
        })
    }
}