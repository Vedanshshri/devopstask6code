job("task6_DSL_job1"){
        description("this job will copy the file in you os version and push image to docker hub")
        scm {
                 github('https://github.com/Vedanshshri/devops_task6_code_repo.git' , 'master')
             }
        triggers {
                scm("* * * * *")
                
        }

        steps {
        shell('''
sudo cp -rvf  *   /root/webpages/
if sudo ls /root/webpages | grep .html
then
  cp -rvf /root/webpages/*.html  /root/devops_task6/html/pages/*
  sudo docker build -t vedanshshrivastava/html_os_1:v1 /root/devops_task6/html/.
  sudo docker push vedanshshrivastava/html_os_1:v1
else 
  cp -rvf /root/webpages/*.php  /root/devops_task6/php/pages/*
  sudo docker build -t vedanshshrivastava/php_os_1:v1 /root/devops_task6/php/.
  sudo docker push vedanshshrivastava/php_os_1:v1
''')
      }
}
job("task6_DSL_job2") {
 triggers {
 upstream('task6_DSL_job1', 'SUCCESS')
 }

job("task6_DSL_job3") {
 triggers {
 upstream('task6_DSL_job2', 'SUCCESS')
 }
 steps {
 shell('''
if sudo kubectl get deploy | grep html_os 
then 
  pass
else
  sudo kubectl create -f /root/devops_task6/task6_html.yaml
fi
if sudo kubectl get deploy | grep php_os 
then 
  pass
else
  sudo kubectl create -f /root/devops_task6/task6_php.yaml
fi  ''')
  }
}
job("task6_DSL_job4") {
  description ("It will test if pod is running else send a mail")
  
  triggers {
    upstream('task6_DSL_job2', 'SUCCESS')
  }
  steps {
    shell('''
if sudo kubectl get deployment | grep html_os
then
echo "send html_os to production"
else
echo "sending html_os back to developer"
exit 1
fi
if sudo kubectl get deployment | grep php_os
then
echo "send php_os to production"
else
echo "sending php_os back to developer"
exit 1
fi
''')
  }
}


buildPipelineView("task6_DSL_BPV") {
  filterBuildQueue(true)
  filterExecutors(false)
  title('task6_DSL_BPV')
  displayedBuilds(1)
  selectedJob('task6_DSL_job1')
  alwaysAllowManualTrigger(false)
  showPipelineParameters(true)
  refreshFrequency(1)
}
}