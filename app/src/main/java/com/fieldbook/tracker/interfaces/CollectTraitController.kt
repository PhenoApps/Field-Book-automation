package com.fieldbook.tracker.interfaces

import com.fieldbook.tracker.traits.BaseTraitLayout
import com.fieldbook.tracker.traits.LayoutCollections

interface CollectTraitController: CollectController {
    fun validateData(): Boolean
    fun getTraitLayouts(): LayoutCollections
    fun isCyclingTraitsAdvances(): Boolean
    fun refreshLock()
    fun inflateTrait(layout: BaseTraitLayout)
}