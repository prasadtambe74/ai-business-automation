package org.example.aibusinessautomation.intent;

import org.springframework.stereotype.Service;

@Service
public class IntentRouter {

    public String route(Intent intent) {

        return switch (intent) {

            case GREETING -> "GREETING_FLOW";

            case FAQ -> "FAQ_FLOW";

            case LEAD -> "LEAD_FLOW";

            case PRODUCT_SEARCH -> "PRODUCT_SEARCH_FLOW";

            case ORDER_STATUS -> "ORDER_STATUS_FLOW";

            case SUPPORT -> "SUPPORT_FLOW";

            case APPOINTMENT -> "APPOINTMENT_FLOW";

            case HUMAN_HANDOFF -> "HUMAN_HANDOFF_FLOW";

            case GOODBYE -> "GOODBYE_FLOW";

            case UNKNOWN -> "UNKNOWN_FLOW";
        };
    }
}