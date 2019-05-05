package client.config;

import client.definitions.AMessagePolicy;
import client.policies.ManhattanNearbyPolicy;
import client.state.State;
import client.policies.BroadcastPolicy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagePolicy {

    public static AMessagePolicy parseMessagePolicy(String policy, State initialState) throws UnknownMessagePolicyException {
        if (policy.equals("broadcast")) {
            return new BroadcastPolicy(initialState);
        }

        Pattern manhattanNearby = Pattern.compile("manhattan-nearby\\((\\d+)\\)");
        Matcher matcher = manhattanNearby.matcher(policy);
        if (matcher.matches()) {
            String maxDistanceStr = matcher.group(1);
            int maxDistance = Integer.parseInt(maxDistanceStr);
            return new ManhattanNearbyPolicy(initialState, maxDistance);
        }

        throw new UnknownMessagePolicyException();
    }

}
