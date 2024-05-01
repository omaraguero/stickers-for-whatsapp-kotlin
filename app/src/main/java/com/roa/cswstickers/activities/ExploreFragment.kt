package com.roa.cswstickers.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roa.cswstickers.R

class ExploreFragment : Fragment() {

    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Si hay inicializaciones específicas en el método onCreate de Java, puedes hacerlas aquí
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de este fragmento
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }
}
