package id.co.awan.walle.constant

enum class MidtransTransactionStatus {
    /**
     * Transaction is created and available/waiting to be paid by customer at the payment provider (ATM/Internet banking/E-Wallet app/store).
     * For card payment method: waiting for customer to complete (and card issuer to validate) 3DS/OTP process.
     */
    PENDING,

    /**
     * Transaction is successful and credit card balance is captured successfully.
     * If no action is taken by you, the transaction will be successfully settled on the next day and transaction status will change to settlement.
     * It is safe to assume a successful payment.
     */
    CAPTURE,

    /**
     * Transaction is successfully settled. Funds have been received.
     */
    SETTLEMENT,

    /**
     * The credentials used for payment are rejected by the payment provider or Midtrans Fraud Detection System (FDS).
     * To know the reason and details for denied transaction, see the <i>status_message</i> field in the response.
     */
    DENY,

    /**
     * Transaction is cancelled. Can be triggered by Midtrans or merchant themselves.
     * Cancelled transaction can be caused by various reasons:
     * 1. Capture transaction is cancelled before Settlement.
     */
    CANCEL,

    /**
     * Transaction no longer available to be paid or processed, because the payment is not completed within the expiry time period.
     */
    EXPIRE,

    /**
     * The transaction status is caused by unexpected error during transaction processing.
     * Failure transaction can be caused by various reasons, but mostly it is caused when bank fails to respond.
     * This occurs rarely.
     */
    FAILURE,

    /**
     * Transaction is marked to be refunded. Refund can be requested by Merchant.
     */
    REFUND,

    /**
     * Transaction is marked to be charged back.
     */
    CHARGEBACK,

    /**
     * Transaction is marked to be partially refunded.
     */
    PARTIAL_REFUND,

    /**
     * Transaction is marked to be partially charged back.
     */
    PARTIAL_CHARGEBACK,

    /**
     * Only available specifically only if you are using pre-authorize feature for card transactions (an advanced feature that you will not have by default,so in most cases are safe to ignore).
     * Transaction is successful and card balance is reserved (authorized) successfully.
     * You can later perform API “capture” to change it into capture, or if no action is taken will be auto released.
     * Depending on your business use case, you may assume authorize status as a successful transaction.
     */
    AUTHORIZE;

}