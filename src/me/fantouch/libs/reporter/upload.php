<?php 

if($_FILES['userfile']['error']>0)  {//首先检查是否有错误  
 echo "错误：";  
     switch ($_FILES['userfile']['error'])  {//根据错误的代码来显示不同的错误信息  
       case 1 : echo '上传的文件大小超过了服务器限制的文本大小。';break;  
       case 2 : echo '上传的文件大小超过了HTML表单的最大值。'   ;break;  
       case 3 : echo '文件只上传了一部分，文件不完整。';break;  
       case 4 : echo '没有选择要上传的文件。';break;  
     }  
     exit;  
   }  
//判定上传文件的类型  
  //  if($_FILES['userfile']['type']!='text/plain')  
  //  {  
  //   echo '错误：文件格式不正确。只能上传文本格式的文件。';  
  //   exit;  
  // }  

    //如果没有任何错误则把文件移动到指定的地方  
   $upfile='/Users/Shared/'.$_FILES['userfile']['name'];  
   $_SESSION["file"] = $upfile;  

if(is_uploaded_file($_FILES['userfile']['tmp_name']))  {//判断是否是通过http post 上传的  
  if(!move_uploaded_file($_FILES['userfile']['tmp_name'],$upfile))  {  
   echo '错误：无法把文件移动到指定的位置。';  
   exit;  
 }  
} else {  
 echo '错误：该文件可能不是从HTTP post 方式上传的：';  
 echo $_FILES['userfile']['name'];  
 exit;  
}  

echo '文件已成功上传。<br>';  

echo '<br>';  
  //浏览目录内容  
$current_dir='/Users/Shared/';  
   $dir=opendir($current_dir); //打开需要浏览的目录  
   echo "<p>Upload directory is $current_dir</p>";  
   echo '<p>Directory Listing:</p><ul>';  

   ?>  


   <table width="800" height="26" border="1">  
     <tr>  
      <td>文件名</td>  
      <td>大小</td>  
      <td>文件类型</td>  
      <td>创建日期</td>  
      <td>修改日期</td>  
    </tr>  

    <?php  

    while($file=readdir($dir)) {
     echo"  <tr> 
     <td>" .$file."</td>";  
     $file=$current_dir.$file;  
     echo "<td>".filesize($file)."</td>
     <td>".filetype($file)."</td>
     <td>".date('j F Y H:i',fileatime($file))."</td>
     <td>".date('j F Y H:i',filemtime($file))."</td></tr>";  
   }  
   
   closedir($dir);   
   echo '<pre>';  
   $result=escapeshellarg(system(escapeshellcmd("ls -la $current_dir")));  
   foreach ($result as $line);  
   echo "$line /n";  
   echo "</pre>";  
   echo '<br>';  

   ?>  
 </table>