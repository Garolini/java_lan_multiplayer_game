package io.github.java_lan_multiplayer;

import io.github.java_lan_multiplayer.server.model.ShipBoard;

import static io.github.java_lan_multiplayer.server.model.BoardType.LEVEL_TWO;

public class DefaultShipBoards {

    // helper class for generating default shipboards.

    static public ShipBoard defaultShipBoard(int id){
        // 1st ship
        ShipBoard shipBoard1 = new ShipBoard(LEVEL_TWO);

        shipBoard1.setTile(4,2,58,0);
        shipBoard1.setTile(5,2,45,0);
        shipBoard1.setTile(6,2,26,3);
        shipBoard1.setTile(2,2,46,3);
        shipBoard1.setTile(1,2,132,0);
        shipBoard1.setTile(0,2,63,2);
        shipBoard1.setTile(1,1,104,0);
        shipBoard1.setTile(2,1,30,1);
        shipBoard1.setTile(3,1,59,0);
        shipBoard1.setTile(4,1,120,0);
        shipBoard1.setTile(5,1,131,0);
        shipBoard1.setTile(2,0,96,0);
        shipBoard1.setTile(0,3,8,2);
        shipBoard1.setTile(1,3,148,0);
        shipBoard1.setTile(2,3,56,3);
        shipBoard1.setTile(3,3,80,0);
        shipBoard1.setTile(4,3,49,2);
        shipBoard1.setTile(5,3,43,2);
        shipBoard1.setTile(6,3,14,3);
        shipBoard1.setTile(0,4,67,0);
        shipBoard1.setTile(1,4,78,0);
        shipBoard1.setTile(2,4,93,0);
        shipBoard1.setTile(4,4,88,0);
        shipBoard1.setTile(5,4,85,0);
        shipBoard1.setTile(6,4,84,0);

        // 2nd ship
        ShipBoard shipBoard2 = new ShipBoard(LEVEL_TWO);

        shipBoard2.setTile(3,3,80,0);
        shipBoard2.setTile(3,1,105,0);
        shipBoard2.setTile(4,2,144,0);
        shipBoard2.setTile(5,2,40,2);
        shipBoard2.setTile(5,1,119,0);
        shipBoard2.setTile(6,2,113,0);
        shipBoard2.setTile(4,3,24,3);
        shipBoard2.setTile(2,2,149,2);
        shipBoard2.setTile(4,4,70,0);
        shipBoard2.setTile(5,3,2,3);
        shipBoard2.setTile(6,3,21,0);
        shipBoard2.setTile(5,4,75,0);
        shipBoard2.setTile(6,4,13,3);
        shipBoard2.setTile(4,1,17,3);
        shipBoard2.setTile(4,0,96,0);
        shipBoard2.setTile(2,1,53,3);
        shipBoard2.setTile(2,0,125,0);
        shipBoard2.setTile(1,2,32,3);
        shipBoard2.setTile(1,3,46,0);
        shipBoard2.setTile(0,3,136,2);
        shipBoard2.setTile(1,1,98,3);
        shipBoard2.setTile(2,3,88,0);
        shipBoard2.setTile(1,4,78,0);
        shipBoard2.setTile(0,4,67,0);

        //3rd ship
        ShipBoard shipBoard3 = new ShipBoard(LEVEL_TWO);

        shipBoard3.setTile(4,2,32,0);
        shipBoard3.setTile(3,1,122,0);
        shipBoard3.setTile(3,3,102,2);
        shipBoard3.setTile(2,2,41,2);
        shipBoard3.setTile(2,3,146,0);
        shipBoard3.setTile(4,3,136,1);
        shipBoard3.setTile(1,3,6,1);
        shipBoard3.setTile(4,4,73,0);
        shipBoard3.setTile(5,4,36,0);
        shipBoard3.setTile(6,4,137,0);
        shipBoard3.setTile(6,3,38,1);
        shipBoard3.setTile(5,3,31,2);
        shipBoard3.setTile(6,2,103,1);
        shipBoard3.setTile(5,2,58,1);
        shipBoard3.setTile(5,1,128,0);
        shipBoard3.setTile(4,1,1,0);
        shipBoard3.setTile(4,0,131,3);
        shipBoard3.setTile(2,4,95,0);
        shipBoard3.setTile(1,4,15,3);
        shipBoard3.setTile(0,4,88,0);
        shipBoard3.setTile(0,3,126,3);
        shipBoard3.setTile(0,2,108,3);
        shipBoard3.setTile(1,2,46,0);
        shipBoard3.setTile(1,1,102,1);

        // 4th shipB
        ShipBoard shipBoard4 = new ShipBoard(LEVEL_TWO);

        shipBoard4.setTile(4,2,33,0);
        shipBoard4.setTile(4,1,121,0);
        shipBoard4.setTile(5,2,61,0);
        shipBoard4.setTile(6,2,8,0);
        shipBoard4.setTile(2,2,6,0);
        shipBoard4.setTile(3,1,128,0);
        shipBoard4.setTile(2,1,18,1);
        shipBoard4.setTile(2,0,16,3);
        shipBoard4.setTile(5,1,101,0);
        shipBoard4.setTile(4,3,46,0);
        shipBoard4.setTile(4,4,140,1);
        shipBoard4.setTile(5,3,74,0);
        shipBoard4.setTile(6,3,13,3);
        shipBoard4.setTile(1,1,120,0);
        shipBoard4.setTile(3,3,76,0);
        shipBoard4.setTile(2,3,50,1);
        shipBoard4.setTile(2,4,95,0);
        shipBoard4.setTile(1,4,79,0);
        shipBoard4.setTile(0,4,103,0);
        shipBoard4.setTile(1,2,10,2);
        shipBoard4.setTile(1,3,65,1);

        if(id == 1) return shipBoard1;
        if(id == 2) return shipBoard2;
        if(id == 3) return shipBoard3;
        if(id == 4) return shipBoard4;
        throw new IllegalArgumentException("invalid id: " + id + ".");
    }
}
