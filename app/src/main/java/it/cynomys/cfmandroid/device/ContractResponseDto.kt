package it.cynomys.cfmandroid.device

import java.util.UUID

data class ContractResponseDto(
    val ContractId: String,
    val CustomerId: String,
    val SalesforceId: String,
    val CurrencyIsoCode: String,
    val StartDate: String,
    val EndDate: String,
    val ContractTerm: Int,
    val Status: String,
    val ActivatedDate: String?,
    val StatusCode: String,
    val ContractNumber: String,
    val NextPaymentDate: String?,
    val Product: String?,
    val Billing_Address: String,
    val ConntractType: String,
    val Country: String,
    val ShipmentContactPerson: String?,
    val ShipmentContractEmail: String?,
    val ShipmentContractPhone: String?,
    val DashboardUser: String?
)

data class License(
    val id: UUID,
    val type: String, // "device" or "silo"
    val name: String, // "core", "performance", "ultimate"
    val inUse: Boolean
)

data class Contract(
    val id: UUID,
    val contractResponseDto: ContractResponseDto,
    val licenses: List<License>,
    val creator: String,
    val activationEmailSent: Boolean
)