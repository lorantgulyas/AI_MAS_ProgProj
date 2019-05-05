package client.config;

import client.definitions.AMessagePolicy;
import client.state.State;
import client.policies.BroadcastPolicy;

public class MessagePolicy {

    public static AMessagePolicy parseMessagePolicy(String policy, State initialState) throws UnknownMessagePolicyException {
        switch (policy) {
            case "broadcast":
                return new BroadcastPolicy(initialState);
            default:
                throw new UnknownMessagePolicyException();
        }
    }

}
