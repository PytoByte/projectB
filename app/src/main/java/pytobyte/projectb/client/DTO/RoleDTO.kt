package pytobyte.projectb.client.DTO

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class RoleDTO(
    val uuid: String,
    val displayName: String,
    val description: String,
    val displayIcon : String,
    val assetPath: String
)