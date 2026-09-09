package com.animalloo.ui.common;

import androidx.fragment.app.Fragment;

/**
 * Base fragment for consistent lifecycle handling.
 */
public abstract class BaseFragment extends Fragment {
    // Subclasses manage ViewBinding and set binding = null in onDestroyView().
}
