package com.trivago.monotony.users;

import java.util.List;

public record UserProfileResponse(
        UserDetails userDetails,
        List<EnrichedOrder> recentOrders,
        int completedOrderCount
) {
}
