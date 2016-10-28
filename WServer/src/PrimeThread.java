import java.net.Socket;
import java.util.ArrayList; //en cas de asc=truee
import java.util.zip.*;
import java.io.*;
/** 
 * La classe PrimeThread implementa un runnable (thread), 
 * i la seva funcio es llegir una peticio del client
 * i servir-la.
 */
public class PrimeThread implements Runnable{
	protected Socket socket2;
	protected InputStream is;
	protected OutputStream os;
	protected FileInputStream fis;
	protected String fileName;
	protected AsciiInputStream ais;
	protected GZIPOutputStream gzos;
	protected ZipOutputStream zos;
	protected ZipEntry ze;
	/**
	 * Constructor de la classe PrimeThread 
	 * @param  socket2 el socket utilitzat per comunicar-se amb el client
	 * 
	 */
	public PrimeThread(Socket socket2){
		this.socket2 = socket2;
	}
	/**
	 * Funcio que executa cada PrimeThread quan es cridat amb la funcio start a 
	 * la classe WServer
	 * 
	 */
	public void run() {
		try{
			is = socket2.getInputStream();
			os = socket2.getOutputStream();
			InputStreamReader info = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(info);
			Integer option = readPetition(br);
			switch(option){
			case 0: //zip+gzip+asc
				ais = new AsciiInputStream(fis);
				gzos = new GZIPOutputStream(os);
				zos = new ZipOutputStream(gzos);
				ZipEntry ze = new ZipEntry(fileName+".asc"); 
				zos.putNextEntry(ze); 
				send(ais,zos);
				zos.finish();
				zos.close();
				break;
			case 1: //zip+gzip
				gzos = new GZIPOutputStream(os);
				ZipOutputStream zos = new ZipOutputStream(gzos);
				ze = new ZipEntry(fileName); 
				zos.putNextEntry(ze); 
				send(fis,zos);
				zos.finish();
				zos.close();
				break;
			case 2: //gzip+asc
				ais = new AsciiInputStream(fis);
				gzos = new GZIPOutputStream(os);
				send(ais,gzos);	
				ais.close();
				gzos.close();
				break;
			case 3: //zip+asc
				ais= new AsciiInputStream(fis);
				zos = new ZipOutputStream(os);
				ze = new ZipEntry(fileName+".asc"); 
				zos.putNextEntry(ze); 
				send(ais,zos);
				ais.close();
				zos.finish();
				zos.close();
				break;
			case 4: //asc
				ais = new AsciiInputStream(fis);
				send(ais,os);
				ais.close();
				os.close();
				break;
			case 5: //zip
				zos = new ZipOutputStream(os);
				ze= new ZipEntry(fileName); 
				zos.putNextEntry(ze); 
				send(fis,zos);
				zos.closeEntry();
				zos.close();
				break;
			case 6: //gzip
				gzos = new GZIPOutputStream(os);
				send(fis,gzos);
				fis.close();
				gzos.close();
				break;
			case 7: //arxiu simple
				send(fis,os);
				fis.close();
				os.close();
			}
		}catch (FileNotFoundException fnf){
			try {
				os.write("HTTP/1.1 404 Not Found \n\n".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
		}
		finally{
			try{
				os.close();
				socket2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		}
	}
	
	
	/**
	 * Aquesta funcio envia els bytes que va llegint per el InputStream
	 * cap al OutputStream perque aquest els envii cap al client.
	 *
	 * @param  in  InputStream utilitzat per llegir els bytes a enviar.
	 * @param  out OutputStream utilitzat per enciar els bytes a escriure.
	 *
	 */
	public void send(InputStream in, OutputStream out) throws IOException{
		int carac;
		carac = in.read();
		while (carac != -1){
			out.write(carac);
			carac = in.read();
		}
	}
	/**
	 * Aquesta funcio llegeix la petiicio del client utilitzant un BufferedReader,
	 * i a partir del string obtingut obte les opcions i el nom del fitxer demanat.
	 *
	 * @param br BufferedReader utilitzat per llegir la peticio del client.
	 * @return option Integer que indica quin tipus de funcio hem d'executar.
	 *
	 */
	public Integer readPetition(BufferedReader br) throws IOException{
		String petition = br.readLine();
		String type;
		Integer option;
		petition = petition.substring(petition.indexOf("/"),petition.indexOf("H")-1);
		if (petition.contains("?")){  //demana opció, exemple: index.html?asc=true&gzip=true&zip=true
			fileName = createFile(petition, true); //crear arxiu
			type = petition.substring(petition.indexOf(".")+1,petition.indexOf("?")); //obtenim el tipu d'arxiu
			petition = petition.substring(petition.indexOf("?")+1); //petiton passa a ser el string de opcions
			option = checkOptions(petition, type, fileName);
			
		}
		else {//no vol opció, exemple: /index.html
			fileName = createFile(petition, false);//crear arxiu
			type = petition.substring(petition.indexOf(".")+1); //html
			header(type,fileName);
			option = 7;
		}
		return option;
		
	}
	/**
	 * Aquesta funcio reb un string amb les opcions i es la encarregada de obtenir exactament
	 * quines opcions ha demanat el client per a cert arxiu.
	 *
	 * @param  petition String que conte la peticio del client demanant certes opcions.
	 * @param type String que conte el tipus de arxiu.
	 * @param fileName String que conte el nom de l'arxiu.
	 * @return option Integer que indica quin tipus de funcio hem d'executar.
	 *
	 */
	public Integer checkOptions(String petition, String type, String fileName) throws IOException{
		String auxiliar = petition;
		Integer option = 10;
		if (auxiliar.contains("&")){
			String[] options;
			options = petition.split("&"); //cada opcio per separat, nomes utilitzat per poder contar el num de opcions
			ArrayList<String> opt = new ArrayList();
			//opcions possibles
			opt.add("asc=true");
			opt.add("gzip=true");
			opt.add("zip=true");
			boolean brk = false;
			if (options.length == 3){
				for (int i = 0; i< 3; i++){
					if (opt.contains(options[i])){
						opt.remove(options[i]);
					}
					else{
						break;
					}
				}
				if (opt.isEmpty() && brk == false){
					header("zip+gzip+asc", fileName);
					option = 0; //zip+gzip+asc
				}
			}
			else if (options.length == 2){
//				opt.clear();
//				//afegim les possibles opcions
//				opt.add("asc=true");
//				opt.add("gzip=true");
//				opt.add("zip=true");
				for (int i = 0; i< 2; i++){
					if (opt.contains(options[i])){
						opt.remove(options[i]);
					}
					else{
						brk = true;
						break;
					}
				}
				if (!brk){ //si no hi ha hagut break, es a dir tot ha anat okey.
					if (opt.contains("asc=true")){ //si a la llista queda asc=true, vol dir que gzip i zip han estat seleccionats
						header("zip+gzip", fileName);
						option = 1; //zip+gzip
					}
					else if (opt.contains("zip=true")){
						header("gzip+asc", fileName);
						option = 2; //gzip+asc
					}
					else if (opt.contains("gzip=true")){
						header("zip+asc", fileName);
						option = 3; //zip+asc
					}
				}
				
			}
		}
		else { // 1 opcio
			if (auxiliar.contentEquals("asc=true") && type.contentEquals("html")){
				header("asc", fileName);
				option = 4; //asc
				
			}
			else if (auxiliar.contentEquals("zip=true")){
				header("zip", fileName);
				option = 5; //zip
				
			}
			else if (auxiliar.contentEquals("gzip=true")){
				header("gzip", fileName);
				option = 6; //gzip
			}
		}
		return option;
	}
	/**
	 * Aquesta funcio es l'encarregada de crear l'arxiu que s'ha d'enviar.
	 *
	 * @param  petition String que conte la peticio del client.
	 * @param  withOption Boolea que indica si s'ha demanat opcio o no.
	 * @return retorna el nom de l'arxiu creat.
	 *
	 */
	public String createFile(String petition, Boolean withOption) throws FileNotFoundException{
		File file;
		if (withOption){
			file = new File(petition.substring(petition.indexOf("/")+1,petition.indexOf("?")));
			fis = new FileInputStream(file);
		}
		else {
			file = new File(petition.substring(petition.indexOf("/")+1)); 
			fis = new FileInputStream(file);
		}
		return file.getName();
		
	}
	/**
	 * Aquesta funcio es l'encarregada de escriure els headers necessaris
	 * per comunicar-se amb el client per fer que es pugui servir la peticio
	 * sense problemes.
	 *
	 * @param  fileType String que conte el tipus d'arxiu a tractar.
	 * @param  fileName String que conte el nom de l'arxiu.
	 *
	 */
	public void header(String fileType, String fileName) throws IOException{
		String addedExtension = "";
		if (fileType.contentEquals("html")){
			fileType = "text/html";
		}
		else if (fileType.contentEquals("zip+gzip+asc")){
			fileType = "application/x-gzip";
			addedExtension = ".asc.zip.gz";
		}
		else if (fileType.contentEquals("zip+asc")){
			fileType = "application/zip";
			addedExtension = ".asc.zip";
		}
		else if (fileType.contentEquals("gzip+asc")){
			fileType = "application/x-gzip";
			addedExtension = ".asc.gz";
		}
		else if (fileType.contentEquals("asc")){
			fileType = "text/plain";
			addedExtension = ".asc";
		}
		else if (fileType.contentEquals("zip")){
			fileType = "application/zip";
			addedExtension = ".zip";
		}
		else if (fileType.contentEquals("gzip")){
			fileType = "application/x-gzip";
			addedExtension = ".gz";
		}
		else if (fileType.contentEquals("zip+gzip")){
			fileType = "application/x-gzip";
			addedExtension = ".zip.gz";
		}
		else if (fileType.contentEquals("txt")){
			fileType = "text/plain";
		}
		else if (fileType.contentEquals("gif")){
			fileType = "image/gif";
		}
		else if (fileType.contentEquals("jpeg")){
			fileType = "image/jpeg";
		}
		else if (fileType.contentEquals("png")){
			fileType = "image/png";
		}
		else if (fileType.contentEquals("xml")){
			fileType = "application/xml";
		}
		else {
			fileType = "application/octet-stream";
		}
		
		String answer = "HTTP/1.1 200 OK\n" + "Content-Type: " + fileType + "\n" + "Content-Disposition: filename=" + "\"" + fileName + addedExtension + "\"" + "\n\n";
		os.write(answer.getBytes());
	}
}