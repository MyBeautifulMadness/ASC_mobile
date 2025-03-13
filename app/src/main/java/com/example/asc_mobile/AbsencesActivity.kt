package com.example.asc_mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AbsencesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absences)
        setupBottomNavigation(R.id.nav_absences)
    }
}