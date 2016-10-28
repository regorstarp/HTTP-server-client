import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
/** 
 * La classe AsciiInputStream es la encarregada de filtrar
 * el contingut d'un arxiu html i eliminar tot el que estigui entre
 * els caracters '<' i '>'
 */
public class AsciiInputStream extends FilterInputStream {
	protected Integer carac;
	protected Integer contador;
	public AsciiInputStream(InputStream in){
		super(in);
		
	}
	/** 
	 * La funcio read es la encarregada de analitzar cada 
	 * caracter que llegeix i avaluar si s'ha de eliminar
	 * o no.
	 * @return carac Integer del caracter a retornar.
	 */
	public int read() throws IOException{
		//return super.read();
		contador = 0;
		carac = in.read();
		while (carac == 60){ //trobem <
			contador = contador + 1; // hem trobat el primer <
			while (carac != 62 & contador > 0){ // esperem a trobar tans > com <
				carac = in.read();
				if (carac == 60){ // hem trobat un altre <
					contador = contador + 1;
				}
				else if (carac == 62){ // hem trobat un >
					contador = contador - 1; 
				}
				
			}
			
			carac = in.read();
		}
		return carac;
		
	}
}
