package ph.edu.benilde.fbms.data

data class ActivationRequest(
    val activationToken: String
)

data class ActivationEnvelope(
    val success: Boolean,
    val message: String,
    val data: ActivationData? = null,
    val errors: List<ApiFieldError>? = null
)

data class ActivationData(
    val deviceSecret: String,
    val tablet: TabletInfo
)

data class TabletInfo(
    val deviceCode: String,
    val deviceName: String
)

data class ApiFieldError(
    val field: String,
    val message: String
)
