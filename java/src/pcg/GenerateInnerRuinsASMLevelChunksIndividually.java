/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pcg;

import java.io.File;
import java.io.PrintStream;

/**
 *
 * @author santi
 */
public class GenerateInnerRuinsASMLevelChunksIndividually {
    public static final int CHUNK_FG_TYPE1 = 1;
    public static final int CHUNK_FG_TYPE2 = 2;
    public static final int CHUNK_FG_TYPE3 = 3;
    public static final int CHUNK_FG_TYPE4 = 4;
    public static final int CHUNK_FG_TYPE5 = 5;
    public static final int CHUNK_FG_TYPE6 = 6;
    public static final int CHUNK_BACKGROUND = 7;
    public static final int CHUNK_DOUBLE_ROOM = 8;

    public static final int CHUNK_TOP = 128;
    public static final int CHUNK_BOTTOM = 64;
    
    public static void main(String args[]) throws Exception {
        String prefix = "innerruins";
        String chunks[]={"data/rooms-inner-ruins/room1-1.tmx",
                         "data/rooms-inner-ruins/room1-2.tmx",
                         "data/rooms-inner-ruins/room1-3.tmx",
                         "data/rooms-inner-ruins/room2-1.tmx",
                         "data/rooms-inner-ruins/room2-2.tmx",
                         "data/rooms-inner-ruins/room2-3.tmx",
                         "data/rooms-inner-ruins/room3-1.tmx",
                         "data/rooms-inner-ruins/room3-2.tmx",
                         "data/rooms-inner-ruins/room3-3.tmx",
                         "data/rooms-inner-ruins/room4-1.tmx",
//                         "data/rooms-inner-ruins/room4-2.tmx",
//                         "data/rooms-inner-ruins/room4-3.tmx",
                         "data/rooms-inner-ruins/room5-1.tmx",
                         "data/rooms-inner-ruins/room5-2.tmx",
                         "data/rooms-inner-ruins/room5-3.tmx",
                         "data/rooms-inner-ruins/room6-1.tmx",
                         "data/rooms-inner-ruins/room6-2.tmx",
                         "data/rooms-inner-ruins/room6-3.tmx",
                         "data/rooms-inner-ruins/room1-bottom-1.tmx",
                         "data/rooms-inner-ruins/room1-bottom-2.tmx",
                         "data/rooms-inner-ruins/doubleroom1.tmx",
                         "data/rooms-inner-ruins/doubleroom2.tmx"};
        String chunk_types[] = {
                             "PCG_CHUNK_FG_TYPE1","PCG_CHUNK_FG_TYPE1","PCG_CHUNK_FG_TYPE1",
                             "PCG_CHUNK_FG_TYPE2","PCG_CHUNK_FG_TYPE2","PCG_CHUNK_FG_TYPE2",
                             "PCG_CHUNK_FG_TYPE3","PCG_CHUNK_FG_TYPE3","PCG_CHUNK_FG_TYPE3",
                             "PCG_CHUNK_FG_TYPE4",//"PCG_CHUNK_FG_TYPE4","PCG_CHUNK_FG_TYPE4",
                             "PCG_CHUNK_FG_TYPE5","PCG_CHUNK_FG_TYPE5","PCG_CHUNK_FG_TYPE5",
                             "PCG_CHUNK_FG_TYPE6","PCG_CHUNK_FG_TYPE6","PCG_CHUNK_FG_TYPE6",
                             "PCG_CHUNK_FG_TYPE1 + PCG_CHUNK_BOTTOM","PCG_CHUNK_FG_TYPE1 + PCG_CHUNK_BOTTOM",
                             "PCG_CHUNK_DOUBLE_ROOM","PCG_CHUNK_DOUBLE_ROOM"
                             };        
        

        PrintStream ps = new PrintStream("src/autogenerated/pcg-" + prefix + "-chunks.asm");
        ps.println(prefix + "_chunk_table:");
        ps.println("  db " + (chunks.length+2) + "    ; the first byte is the number of chunks");
        for(int i = 0;i<chunks.length;i++) {
            ps.println("  db " + chunk_types[i]);
            ps.println("  dw " + prefix+"_chunk_pletter" + i);
        }
        // two rooms reused from outer:
        ps.println("  db PCG_CHUNK_FG_TYPE4");
        ps.println("  dw outerruins_chunk_pletter12");
        ps.println("  db PCG_CHUNK_FG_TYPE4");
        ps.println("  dw outerruins_chunk_pletter13");        
        for(int i = 0;i<chunks.length;i++) {
            ps.println(prefix+"_chunk_pletter" + i + ":");
            ps.println("  incbin \"pcg-"+prefix+"-chunk"+i+".plt\"");
        }
        ps.flush();
        ps.close();
        for(int i = 0;i<chunks.length;i++) {
            LevelChunk lc = new LevelChunk(chunks[i]);
            PrintStream ps2 = new PrintStream(new File("src/autogenerated/pcg-" + prefix + "-chunk"+i+".asm"));
            translateLevelChunkToASM("innerruins_chunk" + i, lc, ps2);
            ps2.flush();
            ps2.close();
        }
    }
    
    
    public static void translateLevelChunkToASM(String prefix, LevelChunk lc, PrintStream os) {
        os.println("  include \"../spelunk-constants.asm\"");
        os.println("  org #0000");
        os.println(prefix + ":");
//        os.println(prefix + "_width:");
//        os.println("  db " + lc.width);
//        os.println(prefix + "_height:");
//        os.println("  db " + lc.height);
//        os.println(prefix + "_map:");
        for(int i = 0;i<lc.height;i++) {
            os.print("  db " + (lc.layer1[0][i]>0 ? lc.layer1[0][i]-1:0));
            for(int j = 1;j<lc.width;j++) {
                os.print("," + (lc.layer1[j][i]>0 ? lc.layer1[j][i]-1:0));
            }
            os.println("");
        }
        os.println(prefix + "_n_enemies:");
        os.println("  db " + 0);
        os.println(prefix + "_enemies:");
        os.println(prefix + "_n_items:");
        os.println("  db " + lc.items.size());
        os.println(prefix + "_items:");
        for(Item i:lc.items) {
            if (i.types.size()==1) {
                os.println("  db ITEM_"+i.types.get(0).toUpperCase()+","+i.x/8+","+i.y/8+",0");
            } else {
                System.out.println("Compound item in a map should not occur anymore!!");
                /*
                if (i.type_string.equals(":3,bomb:1,rope:1,arrow:1")) {
                    os.println("  db ITEM_PCG_SUPPLY,"+i.x/8+","+i.y/8+",0");
                } else if (i.type_string.equals(":5,bomb:3,rope:3,shield:1,bow:2,arrow:3,boots:1,scubamask:1,boulder:2")) {
                    os.println("  db ITEM_PCG_ANY,"+i.x/8+","+i.y/8+",0");
                } else if (i.type_string.equals("shield:1,bow:1,boots:1,scubamask:1")) {
                    os.println("  db ITEM_PCG_GOOD_ITEM,"+i.x/8+","+i.y/8+",0");
                } else {
                    System.err.println("Unknown item type: " + i.type_string);
                }*/
            }
        }
    }
}