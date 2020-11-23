#!/bin/sh
#https://github.com/visionmedia/deploy/blob/master/bin/deploy
. init.sh

#Function: printLog()
#Param: 

logPath="`pwd`/logs"
function printLog(){
    local errorCode=$?
    local logInfo=$1
    if [ ! -d ${logPath} ];then
        mkdir -p ${logPath}
    fi
    if [ $# -ne 1 ];then
        echo `date +"%Y-%m-%d %H:%M:%S"` "[ERROR] Usage:printLog logInfo" | tee --append ${logPath}/svnRuntimeLog-`date +"%Y-%m-%d"`.txt
        exit 1
    fi
    if [ ${errorCode} -ne 0 ];then
        echo `date +"%Y-%m-%d %H:%M:%S"` "[ERROR] ${logInfo}" | tee --append ${logPath}/svnRuntimeLog-`date +"%Y-%m-%d"`.txt
        return 1
    else
        echo `date +"%Y-%m-%d %H:%M:%S"` "${logInfo}" >> ${logPath}/svnRuntimeLog-`date +"%Y-%m-%d"`.txt
    fi
}

#Function: backup()
#Param: 
function backup(){
    if [ $# -ne 2 ];then
        echo "[ERROR] Usage:backup fileName backupPath"
        printLog "[ERROR] Usage:backup fileName backupPath"
        exit 1
    fi
    local fileName=$1
    local backupPath=$2
    local bakDate=`date +'%Y%m%d'`
    local bakTime=`date +'%H%M'`
    local delTime=`date -d -7day +'%Y%m%d'`
    echo "${backupPath}/${bakDate}/${bakTime}-${fileName}]"
    if [ -d ${backupPath}/${bakDate} ];then
        mv ${fileName} ${backupPath}/${bakDate}/${bakTime}-${fileName}
        printLog "[${backupPath}/${bakDate}/${bakTime}-${fileName}]" 
    else
        mkdir -p ${backupPath}/${bakDate}
        mv ${fileName} ${backupPath}/${bakDate}/${bakTime}-${fileName}
        printLog "[${backupPath}/${bakDate}/${bakTime}-${fileName}]" 
    fi
    echo "[${backupPath}/${delTime}]"
    rm -rf ${backupPath}/${delTime}    
    printLog "[${backupPath}/${delTime}]"
}

function deploy_pkg(){
    if [ $# -ne 3 ];then
        echo "[ERROR] Usage:deploy packageFile delFile projectPath"
        printLog "[ERROR] Usage:deploy packageFile delFile projectPath"
        exit 1
    fi
    local packageFile=$1
    local delFile=$2
    local projectPath=$3
    if [ -f ${packageFile}.tar.gz ];then
        tar zxvf ${packageFile}.tar.gz
        if [ -f ${delFile} ];then
            cat ${delFile} |
            while read row; do
                if [ "${row}" == "noneLine" ];then
                    exit
                elif [ "${row}" != "" ];then 
                    rm -rfv ${projectPath}/${row}
                    printLog "${projectPath}/${row}"
                fi
            done
        fi
        echo "[${projectPath}]"
        chown -R www.www ${packageFile}_*/
        printLog "更改[${packageFile}_*/]权限为www.www" 
        \cp -rfv ${packageFile}_*/* ${projectPath}
        printLog "部署升级包[${packageFile}_*/]至[${projectPath}]" 
        rm -rf ${packageFile}_*
        printLog "删除升级包[${packageFile}_*]"
    else
        printLog "升级包[${packageFile}.tar.gz]不存在!" && exit 1
    fi
}

#Function: update_release_notes()
#Param: 

COMMAND=$1

if [ "COMMAND" = "" ]; then
 echo "
 To deploy, run:
 ./deploy --rollout {artifact.id}
 
 To rollback, run:
 ./deploy --rollback {artifact.id}
 
 If the location where the .deploy file isn't specified, deploy.sh will assume
 "
 exit
fi

# full commands
if [ "$COMMAND" = "--install" ]; then
        echo "Initiating executing of full deploying sequence..." 
        $APP --begin
        $APP --copy
        $APP --replace
        $APP --finish
        echo "Deployment is done"
        exit
elif [ "$COMMAND" = "--update" ]; then
        echo "Initiating executing of update sequence..." 
        $APP --copy
        $APP --replace
        $APP --update-s2
        echo "Update is done"
        exit
elif [ "$COMMAND" = "--help" ]; then
	$APP
	exit
fi


main(){
  DEPLOY_ENV=$1
  DEPLOY_TYPE=$2
  DEPLOY_VER=$3
  if [ -f "$LOCK_FILE" ];then
     shell_log "${SHELL_NAME} is running"
     echo "${SHELL_NAME} is running" && exit
  fi
  shell_lock;
  case $DEPLOY_TYPE in
    deploy)
       get_pkg;
       config_pkg;
       scp_pkg;
       deploy_pkg;
       test_pkg;
       ;;
    rollback)
       rollback
       ;;
    fast-rollback)
       fast_rollback;
       ;;
    rollback-list)
       rollback_list;
       ;;
     *)
       usage;
     esac
     shell_unlock;
}