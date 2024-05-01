package com.roa.cswstickers.activities

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.roa.cswstickers.R
import com.roa.cswstickers.identities.StickerPacksContainer
import com.roa.cswstickers.utils.StickerPacksManager
import com.roa.cswstickers.whatsapp_api.AddStickerPackActivity
import com.sangcomz.fishbun.FishBun
import java.util.Objects

class MainActivity : AddStickerPackActivity() /*CheckRefreshClickListener*/ {
    private var myStickersFragment: MyStickersFragment? = null
    private var exploreFragment: ExploreFragment? = null
    private var createFragment: CreateFragment? = null
    private var fab: FloatingActionButton? = null
    private var bottomAppBar: BottomAppBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)


        bottomAppBar = findViewById(R.id.bottom_app_bar)
        setSupportActionBar(bottomAppBar)
        Fresco.initialize(this)
        setupFragments()
        setFragmento(myStickersFragment)
        StickerPacksManager.stickerPacksContainer =
            StickerPacksContainer("", "", StickerPacksManager.getStickerPacks(this).toMutableList())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            window.setDecorFitsSystemWindows(true)
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }


        fab = findViewById(R.id.fab)
        fab!!.setImageDrawable(
            ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.cusotmcreate,
                null
            )
        )
        fab!!.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                NewStickerPackActivity::class.java
            )
            startActivity(intent)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = getMenuInflater()
        return super.onCreateOptionsMenu(menu)
    }




    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupFragments() {
        val fragmentTransaction: FragmentTransaction =
            getSupportFragmentManager().beginTransaction()
        myStickersFragment = MyStickersFragment()
        exploreFragment = ExploreFragment()
        createFragment = CreateFragment()
        fragmentTransaction.add(R.id.frame_principal, myStickersFragment!!)
        fragmentTransaction.add(R.id.frame_principal, exploreFragment!!)
        fragmentTransaction.add(R.id.frame_principal, createFragment!!)
        fragmentTransaction.hide(exploreFragment!!)
        fragmentTransaction.hide(createFragment!!)
        fragmentTransaction.commit()
    }

    private fun setFragmento(fragmento: Fragment?) {
        val fragmentTransaction: FragmentTransaction =
            getSupportFragmentManager().beginTransaction()
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        if (fragmento === myStickersFragment) {
            fragmentTransaction.hide(exploreFragment!!)
            fragmentTransaction.hide(createFragment!!)
        } else if (fragmento === exploreFragment) {
            fragmentTransaction.hide(myStickersFragment!!)
            fragmentTransaction.hide(createFragment!!)
        } else if (fragmento === createFragment) {
            fragmentTransaction.hide(myStickersFragment!!)
            fragmentTransaction.hide(exploreFragment!!)
        }
        fragmentTransaction.show(fragmento!!)
        fragmentTransaction.commit()
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            System.exit(0)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuItem = item.itemId
        return super.onOptionsItemSelected(item)
    }


}
