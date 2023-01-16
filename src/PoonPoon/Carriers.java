package PoonPoon;

import battlecode.common.*;

public class Carriers extends Base {
    public void run(RobotController rc) throws GameActionException {
        act(rc);
        move(rc);
        act(rc);
    }

    public void act(RobotController rc) throws GameActionException {
        if (!rc.isActionReady())
            return;

        MapLocation nearestHqLoc = findNearest(rc, hqSection, quadSection);
        // attack nearest enemy
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            rc.setIndicatorString("Attacking nearest enemy!");
            attackNearestEnemy(rc, nearbyEnemies);
        }
        if (rc.getLocation().isWithinDistanceSquared(nearestHqLoc, 2)) {
            // transferResources
            if (rc.getWeight() > 0) {
                rc.setIndicatorString("Transferring resources!");
                transferResources(rc, nearestHqLoc);
            }
            // pickup/place anchor
            // TODO: implement anchor stuff
        }
        // collect resources
        MapLocation nearestWell = findNearestWell(rc);
        if (nearestWell != null && rc.canCollectResource(nearestWell, -1)) {
            rc.setIndicatorString("Collecting resource!");
            rc.collectResource(nearestWell, -1);
        }

    }

    public void move(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Is mvt ready? " + rc.isMovementReady());
        if (!rc.isMovementReady())
            return;

        rc.setIndicatorString("Moving...");

        // evade nearest enemy
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            rc.setIndicatorString("Evading enemy!");
            evadeEnemies(rc, nearbyEnemies);
        }
        // explore/return
        if (rc.getWeight() < GameConstants.CARRIER_CAPACITY) {
            MapLocation nearestWell = findNearestWell(rc);
            // If can't collect (out of range) move to well
            if (nearestWell != null && !rc.canCollectResource(nearestWell, -1)) {
                rc.setIndicatorString("Moving to well!" + nearestWell);
                tryMoveTo(rc, nearestWell);
                // If no nearby well, explore
            } else {
                rc.setIndicatorString("Exploring!");
                tryMoveTo(rc, getExploreDirection(rc));
            }
        }
        // return to hq
        else if (rc.getWeight() == GameConstants.CARRIER_CAPACITY) {
            rc.setIndicatorString("Returning to hq!");
            returnToHQ(rc);
        }
    }

    /**
     * Tries to collect resource from well.
     * If can't, then tries to move towards well.
     */
    public void collectOrMoveToWell(RobotController rc, MapLocation wellLoc) throws GameActionException {
        if (rc.canCollectResource(wellLoc, -1)) {
            rc.collectResource(wellLoc, -1);
        } else {
            tryMoveTo(rc, wellLoc);
        }
    }

    public void transferResources(RobotController rc, MapLocation hqLoc) throws GameActionException {
        ResourceType[] allResourceTypes = { ResourceType.MANA, ResourceType.ADAMANTIUM, ResourceType.ELIXIR };
        for (ResourceType resourceType : allResourceTypes) {
            if (rc.canTransferResource(hqLoc, resourceType, rc.getResourceAmount(resourceType))) {
                rc.transferResource(hqLoc, resourceType, rc.getResourceAmount(resourceType));
            }
        }
    }
}
