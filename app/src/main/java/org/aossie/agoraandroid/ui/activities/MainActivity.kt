package org.aossie.agoraandroid.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.bottom_navigation
import kotlinx.android.synthetic.main.activity_main.iv_back
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.tv_title
import org.aossie.agoraandroid.R
import org.aossie.agoraandroid.home.HomeFragment
import org.aossie.agoraandroid.home.ProfileFragment
import org.aossie.agoraandroid.ui.fragments.MoreFragment
import org.aossie.agoraandroid.ui.fragments.elections.ElectionsFragment
import org.aossie.agoraandroid.utilities.SharedPrefs

class MainActivity : AppCompatActivity() {

  private lateinit var navController: NavController
  private lateinit var sharedPrefs: SharedPrefs

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)

    sharedPrefs = SharedPrefs(this@MainActivity)

    val hostFragment = supportFragmentManager.findFragmentById(R.id.host_fragment)
    if (hostFragment is NavHostFragment)
      navController = hostFragment.navController

    navController.addOnDestinationChangedListener { _, destination, _ ->
      setToolbar(destination)
    }

    NavigationUI.setupWithNavController(bottom_navigation, navController)

    val userName = sharedPrefs.userName
    val password = sharedPrefs.pass
    if (userName != null && password != null) {
      navController.navigate(R.id.homeFragment)
    }
  }

  private fun setToolbar(destination: NavDestination) {
    handleBottomNavVisibility(destination.id)
    handleBackButton(destination.id)
    tv_title.text = destination.label
  }

  private fun handleBottomNavVisibility(id: Int) {
    when (id) {
      R.id.profileFragment,
      R.id.homeFragment,
      R.id.electionsFragment,
      R.id.moreFragment
      -> bottom_navigation.visibility = View.VISIBLE
      else -> bottom_navigation.visibility = View.GONE
    }
  }

  override fun onBackPressed() {
    val hostFragment = supportFragmentManager.findFragmentById(R.id.host_fragment)
    if (hostFragment is NavHostFragment) {
      val currentFragment = hostFragment.childFragmentManager.fragments.first()
      when (currentFragment) {
        is HomeFragment -> moveTaskToBack(true)
        is ElectionsFragment,
        is ProfileFragment,
        is MoreFragment -> navController.navigate(R.id.homeFragment)
       else -> super.onBackPressed()
      }
    } else {
      super.onBackPressed()
    }
  }

  private fun handleBackButton(id: Int) {
    when (id) {
      R.id.loginFragment,
      R.id.signUpFragment,
      R.id.forgotPasswordFragment -> iv_back.let {
        it.visibility = View.VISIBLE
        it.setOnClickListener { onBackPressed() }
      }
      else -> iv_back.visibility = View.GONE
    }
  }
}
