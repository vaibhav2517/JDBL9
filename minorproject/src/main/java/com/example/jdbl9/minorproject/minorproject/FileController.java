package com.example.jdbl9.minorproject.minorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
public class FileController {
@Autowired
Test test;


RestTemplate restTemplate=new RestTemplate();

/* Ques 2: Reyrun file by id*/

    @GetMapping(value = "/getfile", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImageWithMediaType(@RequestParam(value="image") int image) throws IOException
    {

        byte[] bytes=restTemplate.getForObject("https://picsum.photos/id/"+image+"/200/300",byte[].class);

        return bytes;
    }

    /*Ques3. Taking list of ids as input and returning zip as output:*/

    @RequestMapping(value="/getFilesById", produces="application/zip")
    public ResponseEntity<Object> zipFile(@RequestParam(value="ids") List<Integer> ids) throws IOException {

        String filename="C:/Users/Vaibhav Sharma/OneDrive/Desktop/newtarget123.zip";
        FileOutputStream fos=new FileOutputStream(filename);
        ZipOutputStream zos=new ZipOutputStream(fos);

        for(Integer id:ids)
        {
            byte[] bytes=restTemplate.getForObject("https://picsum.photos/id/"+id+"/200/300",byte[].class);
            ZipEntry zipEntry=new ZipEntry(id.toString()+"file.jpg");
            zos.putNextEntry(zipEntry);
            zos.write(bytes);


        }

        zos.close();
        fos.close();

        File file=new File(filename);
        InputStreamResource resource=new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers=new HttpHeaders();
        headers.add("Content-Disposition",String.format("attachement; filename=\"%s\"",file.getName()));
        headers.add("Cache-Control","no-cache,no-store,must-revalidate");
        headers.add("Pragma","no-cache");
        headers.add("Expires","0");

        ResponseEntity<Object> responseEntity=ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(MediaType.parseMediaType("application/txt")).body(resource);

        return responseEntity;


    }

    /*Ques1. Request - Zip file
      Response - nothing to return to client, just unzip the contents on the server’s disk
    */

    @RequestMapping(value="/uploadZip",method=RequestMethod.POST,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadFile(@RequestParam("file")MultipartFile file)throws IOException
    {

        String target="C:/Users/Vaibhav Sharma/OneDrive/Desktop/MyFile123";
        File dir=new File(target);

        if(! dir.exists()){dir.mkdir();};

        FileInputStream fis=(FileInputStream) file.getInputStream();
        ZipInputStream zis=new ZipInputStream(fis);
        ZipEntry ze= zis.getNextEntry();

        while(ze !=null)
        {
            String filename=ze.getName();

            File fileObject=new File(target+File.separator+filename);
            fileObject.createNewFile();

            FileOutputStream fos=new FileOutputStream(fileObject);

            System.out.println("Unziping content: of "+filename);

            int temp;
            while((temp=zis.read())!=-1)
            {
                fos.write((byte)temp);
            }

            ze=zis.getNextEntry();
            fos.close();
        }



        return new ResponseEntity<>("File unziped successfully at: "+target,HttpStatus.OK);
    }



}
