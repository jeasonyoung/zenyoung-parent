import Vue from "vue";
import DataDict from "@/util/dict";

import {getDictAllData} from "@/api/sys/dictType";

function install() {
  Vue.use(DataDict, {
    '*': {
      labelField: "label",
      valueField: "value",
      request(dictMeta){
        return getDictAllData(dictMeta.type)
      }
    }
  })
}

export default {
  install
}
