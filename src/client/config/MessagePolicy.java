package client.config;

import client.definitions.ADistance;
import client.definitions.AMessagePolicy;
import client.policies.NearbyPolicy;
import client.policies.PublicNearbyPolicy;
import client.policies.PublicPolicy;
import client.state.State;
import client.policies.BroadcastPolicy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagePolicy {

    public static AMessagePolicy parseMessagePolicy(String policy, State initialState, ADistance distance)
            throws UnknownMessagePolicyException {
        if (policy.equals("broadcast")) {
            return new BroadcastPolicy(initialState);
        }

        if (policy.equals("public")) {
            return new PublicPolicy(initialState);
        }

        Pattern nearby = Pattern.compile("nearby\\((\\d+)\\)");
        Matcher nearbyMatcher = nearby.matcher(policy);
        if (nearbyMatcher.matches()) {
            String maxDistanceStr = nearbyMatcher.group(1);
            int maxDistance = Integer.parseInt(maxDistanceStr);
            return new NearbyPolicy(initialState, maxDistance, distance);
        }

        Pattern publicNearby = Pattern.compile("public-nearby\\((\\d+)\\)");
        Matcher publicMatcher = publicNearby.matcher(policy);
        if (publicMatcher.matches()) {
            String maxDistanceStr = publicMatcher.group(1);
            int maxDistance = Integer.parseInt(maxDistanceStr);
            return new PublicNearbyPolicy(initialState, maxDistance, distance);
        }

        throw new UnknownMessagePolicyException();
    }

}
