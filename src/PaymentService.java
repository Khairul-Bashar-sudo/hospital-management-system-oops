public class PaymentService {

    public PaymentResult processPayment(String paymentMethod, double amount) {
        String normalized = paymentMethod == null ? "" : paymentMethod.trim().toLowerCase();

        if (normalized.equals("card") || normalized.equals("upi") || normalized.equals("netbanking") || normalized.equals("wallet")) {
            return new PaymentResult(
                true,
                "PAID",
                "TXN-" + System.currentTimeMillis(),
                "Payment captured successfully via " + paymentMethod.toUpperCase()
            );
        }

        if (normalized.equals("cash")) {
            return new PaymentResult(
                false,
                "PENDING",
                "",
                "Payment will be collected at the reception desk."
            );
        }

        return new PaymentResult(
            false,
            "FAILED",
            "",
            "Unsupported payment method."
        );
    }

    public static class PaymentResult {
        private final boolean success;
        private final String status;
        private final String transactionId;
        private final String message;

        public PaymentResult(boolean success, String status, String transactionId, String message) {
            this.success = success;
            this.status = status;
            this.transactionId = transactionId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getStatus() {
            return status;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMessage() {
            return message;
        }
    }
}
