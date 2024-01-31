package pytobyte.projectb.client.DTO

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AbilitiesDTO(
    val slot: String,
    val displayName: String,
    val description: String,
    val displayIcon: String
)