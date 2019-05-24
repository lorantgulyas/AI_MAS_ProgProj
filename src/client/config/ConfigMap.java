package client.config;

import java.util.HashMap;

public class ConfigMap {

    public static String getLevelConfig(String levelName){
        HashMap<String, String> levelConfigMap = new HashMap<String, String>();
        levelConfigMap.put("maaimas","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("maavicii", "src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("mabob","");
        levelConfigMap.put("maforthepie", "src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("magronhoff", "src/configs/maa_gs_sp_bc_g.config");
        levelConfigMap.put("magroupname", "");
        levelConfigMap.put("magruppeto", "src/configs/maa_st_sp_bc_g.config");
        levelConfigMap.put("mahelloworl", "");
        levelConfigMap.put("mamasa","src/configs/maa_st_sp_bc_g.config"); // want to see this one
        levelConfigMap.put("mamasai","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("mamkm", "src/configs/maa_st_sup_bc_g.config");
        levelConfigMap.put("manoasark","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("manameless","src/configs/maa_st_sp_bc_g.config");
        levelConfigMap.put("manulpoint","src/configs/maa_st_sp_bc_g.config");
        levelConfigMap.put("maoneonetwo","src/configs/maa_u_sp_bc_g.config");
        levelConfigMap.put("mapopstars","");
        levelConfigMap.put("maregexaz","src/configs/maa_gs_sp_bc_g.config");
        levelConfigMap.put("masoulman","src/configs/maa_u_sp_bc_g.config");
        levelConfigMap.put("mastarfish","src/configs/maa_st_sp_bc_g.config");
        levelConfigMap.put("masubpoena","");
        levelConfigMap.put("mathebteam","");
        levelConfigMap.put("mavisualkei","");
        levelConfigMap.put("mawallz","src/configs/maa_st_sp_bc_g.config"); //want to see
        levelConfigMap.put("madeepurple", "");
        levelConfigMap.put("magthirteen","");
        levelConfigMap.put("magroup","src/configs/maa_gs_sp_bc_g.config");
        levelConfigMap.put("saaimas","");
        levelConfigMap.put("saavicii","");
        levelConfigMap.put("sabob","");
        levelConfigMap.put("saforthepie","");
        levelConfigMap.put("sagronhoff","src/configs/maa_st_sp_bc_g.config");
        levelConfigMap.put("sagroupname","");
        levelConfigMap.put("sagruppeto","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("sahelloworl","src/configs/maa_u_sp_bc_g.config");
        levelConfigMap.put("samasa","");
        levelConfigMap.put("samasai","");
        levelConfigMap.put("samkm","src/configs/maa_st_sp_bc_g.config");
        levelConfigMap.put("sanoasark","src/configs/maa_gs_sp_bc_g.config");
        levelConfigMap.put("sanameless","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("sanulpoint","");
        levelConfigMap.put("saoneonetwo","src/configs/maa_u_sp_bc_g.config");
        levelConfigMap.put("sapopstars","");
        levelConfigMap.put("saregexaz","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("sasoulman","src/configs/maa_gs_sp_bc_g.config");
        levelConfigMap.put("sastarfish","src/configs/maa_u_sp_n4_g.config");
        levelConfigMap.put("sasubpoena","");
        levelConfigMap.put("sathebteam","");
        levelConfigMap.put("savisualkei","");
        levelConfigMap.put("sawallz","src/configs/maa_u_sp_bc_g.config");
        levelConfigMap.put("sadeepurple","src/configs/maa_u_sp_bc_g.config");
        levelConfigMap.put("sagthirteen","");
        levelConfigMap.put("sagroup","src/configs/maa_st_sp_bc_g.config");
        if(levelConfigMap.get(levelName).equals(""))
            return "src/configs/default.config";
        return levelConfigMap.get(levelName);
    }


}
