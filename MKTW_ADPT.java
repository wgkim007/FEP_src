import	java.io.*;
import	java.net.*;
import	java.util.Arrays;

class XML_Send
{
	private final	String	host;
	private final	int	port;

	private String Make_FileFull(String xml_path, String mktw_id) {
		String filename = xml_path + "/" + mktw_id + "R.xml";
		return(filename);
	}

	private String Make_FileName(String mktw_id) {
		String filename = mktw_id + "R.xml";
		return(filename);
	}

	public XML_Send(String host, int port)
	{
		this.host     = host;
		this.port     = port;
	}

	public void File_Send(String mktw_id) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{
		InetAddress 	localhost = InetAddress.getLocalHost();
		BufferedReader  br = null;
		String		fullname = Make_FileFull(".", mktw_id);
		String		filename = Make_FileName(mktw_id);
		
		try {
			br = new BufferedReader(new FileReader(fullname));

			Socket		socket	= new Socket(host, port);
			BufferedWriter	out	= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			System.out.println("File_Send : fullname=" + fullname);
			System.out.println("File_Send : filename=" + filename);

			String	str_flen = String.format("%04d", filename.length());
			out.write(str_flen);

			out.write(filename);

			System.out.println("File_Send : KKK1");
			
			String	line;
			while ( (line = br.readLine()) != null ) {
				System.out.println("Read: " + line);
				out.write(line);
				out.newLine();
			}

			out.close();
			out = null;

			socket.close();
		} catch (IOException msg) {
			System.out.println("ERROR: File_Send BufferedReader " + filename + " " + msg);
		} finally {
			br.close();
		}
	}
}

class XML_Recv implements Runnable
{
	Thread			t;
	private	Socket		socket = null;
	private	ServerSocket	server = null;
	private final	int	port;

	public XML_Recv(int port) {
		this.port = port;
	}

	public void run()
	{
		try {
			server = new ServerSocket(port);
			System.out.println("Server started");
			System.out.println("Waiting for a client");

			while ( true ) {
				socket = server.accept();
				System.out.println("Client accepted");

				BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				char	buffer[] = new char[1024];
				int	count = in.read(buffer, 0, 128);
				int	filenm_len = 0;
				for (int i = 0 ; i < count ; i++) {
					if ( buffer[i] == 0x00 ) {
						filenm_len = i;
						break;
					}
				}

				String	filename = new String(buffer, 0, filenm_len);

				System.out.println("head: [" + filename +"]");
			
				String	fullname = "./" + filename;
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fullname)));

				String	line = "";
				while ( true ) {
					try {
						line = in.readLine();
						System.out.println("data:" + line);
					} catch(IOException msg) {
						System.out.println("ERROR: readLine " + msg);
						break;
					}

					if ( line == null ) {
						break;
					}

					out.println(line);
				}
				System.out.println("Closing connection");

				out.close();
				in.close();
				socket.close();
			}
		} catch(IOException msg) {
			System.out.println("ERROR: XML_Recv " + msg);
		}
	}
}

class ADPT_Proc implements Runnable
{
	Thread			t;
	private final	String	host;
	private final	int	port;
	private final	int	sleep_sec;

	public ADPT_Proc(String host, int port, int sleep_sec) {
		this.host      = host;
		this.port      = port;
		this.sleep_sec = sleep_sec;
	}

	public void run()
	{
		XML_Send	xml_send = new XML_Send(host, port);

		while ( true ) {
			System.out.println("ADPT_Proc Sleep");

			try {
				xml_send.File_Send("201810150000000001");
			} catch(Exception msg) {
				System.out.println("ERROR: ADPT_Proc File_Send " + msg);
			}

			try {
				Thread.sleep(sleep_sec * 1000);
			} catch(InterruptedException msg) {
				System.out.println("ERROR: ADPT_Proc sleep " + msg);
			}
		
		}
	}
}

public class MKTW_ADPT
{
	public static	void main(String[] args) throws Exception
	{
		Thread	th_XMLRecv   = new Thread(new XML_Recv(40020));
		Thread	th_ADPT_Proc = new Thread(new ADPT_Proc("172.27.1.152", 40020, 20));

		th_XMLRecv.start();
		th_ADPT_Proc.start();
	}
}
