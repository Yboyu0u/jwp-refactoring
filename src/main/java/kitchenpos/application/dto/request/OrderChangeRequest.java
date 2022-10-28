package kitchenpos.application.dto.request;

public class OrderChangeRequest {

    private final String orderStatus;

    public OrderChangeRequest(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
