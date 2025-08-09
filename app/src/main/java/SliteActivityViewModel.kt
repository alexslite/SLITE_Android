import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestLightsListUseCase
import kotlinx.coroutines.launch

class SliteActivityViewModel(
    private val storeLatestLightsList: StoreLatestLightsListUseCase
) : ViewModel() {

    fun storeLatestLightsAndClearEffectsSettings() {
        viewModelScope.launch { storeLatestLightsList(true) }
    }
}