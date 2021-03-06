/*
 * Client class of the File Transfer Application connects to the Server class
 * using TCP [connection-oriented protocol] Socket (Socket and ServerSocket classes).
 * Client class receives File options from the Server and sends its response.
 * 
 * The File options are:
 * To List Files of the Server Directory,
 * Filter Files by Department ID,
 * Download files from the Server Directory onto the Client Machine.
 * 
 * Here the File Naming Convention used is: 
 * DepartmentID-AccountID  eg:  1234-123456
 * 
 * @author 04xRaynal
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.net.InetAddress;
import java.net.Socket;

public class FileTransfer_Client {
	private static DataInputStream din;
	private static DataOutputStream dout;
	private static Socket clientSocket;
	
	
	public static void main(String[] args) {
		try {
			clientSocket = new Socket("localhost", 6969);
			//host is 'localhost' when Client and Server are on the same machine, input IpAaddress when both are on different machines
			
			din = new DataInputStream(clientSocket.getInputStream());
			dout = new DataOutputStream(clientSocket.getOutputStream());  
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			String str = "";
			int i = 0;
			
			str = din.readUTF();
			System.out.println("Server: " + str);
			
			dout.writeUTF(br.readLine());
			str = din.readUTF();
			System.out.println("Server: " + str);
			
			
			while(!(i == 3)) {
				str = din.readUTF();
				System.out.println("\nServer: " + str);
				
				do {
					try {
						i = Integer.parseInt(br.readLine());
						
						if(! (1 <= i && i <= 3)) {
							System.out.println("        Please Check your Input!");
						}
					}
					catch(NumberFormatException ex) {
						System.out.println("        Input should be a Number. Please Try again!");
						i = -1;
					}
				} while(! (1 <= i && i <= 3));				//loops until i matches the desired values
				dout.writeInt(i);
				
				
				if(i == 1) {
					do {
						str = din.readUTF();
						System.out.println("\nServer: " + str);
						
						str = br.readLine().trim();				
					
						dout.writeUTF(str);					//writes the inputed fileName
					}while(! (str.length() == 11));			//loops until the length of the input is matched
					
					receiveFile();					//file is received over the network
				}
				else if(i == 2) {
					int j = 0;
					while(!(j == 4)) {
						str = din.readUTF();
						System.out.println("\nServer: " + str);
						
						do {
							try {
								j = Integer.parseInt(br.readLine());
								
								if(! (1 <= j && j <= 4)) {
									System.out.println("        Please Check your Input!");
								}
							}
							catch(NumberFormatException ex) {
								System.out.println("        Input should be a Number. Please Try again!");
								j = -1;
							}
						} while(! (1 <= j && j <= 4));				//loops until j matches the desired values
						dout.writeInt(j);
						
						
						if(j == 1) {
							str = din.readUTF();
							System.out.println("\nServer: " + str);
							
							str = br.readLine();
							dout.writeUTF(str);					//returns String of departmentID
						}
						else if(j == 2) {
							str = din.readUTF();
							System.out.println("\nServer: " + str);
							
							int files_length = din.readInt();				//number of files present is received
							int input_serial_no = 0;
							do {
								try {
									input_serial_no = Integer.parseInt(br.readLine());
								}
								catch(NumberFormatException ex) {
									System.out.println("        Serial No. should be a Number. Please Try again!");
									input_serial_no = -1;
								}
								dout.writeInt(input_serial_no);
							} while(!(input_serial_no <= files_length && input_serial_no >= 0));			//loops until proper input is submitted
							
							receiveFile();					//file is received over the network
						}
						else if(j == 3) {
							do {
								str = din.readUTF();
								System.out.println("\nServer: " + str);
								
								str = br.readLine().trim();
								dout.writeUTF(str);
							}while(false);					//loops until the length is matched
							
							receiveFile();					//file is received over the network
						}
					}
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	private static void receiveFile() {
		int bytesRead = 0, current = 0;
		int fileLength;
		
		try {
			String fileName = din.readUTF();
			fileLength = din.readInt();
			byte[] byteArray = new byte[1024];
			System.out.println("        File size " + fileLength);

			BufferedInputStream bis = new BufferedInputStream(din);

			File file = new File("Client Files\\" + fileName);
			
			//fileFoundFlag is a Flag which denotes the file is present or absent from the Server directory, is present int 0 is sent, else 1
			int fileFoundFlag = din.readInt();
			if(fileFoundFlag == 1)
				return;
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

			do{
				// ??????: TCP read ????????????.
				bytesRead = bis.read(byteArray);
				bos.write(byteArray, 0, bytesRead);
				current += bytesRead;

				System.out.print("Downloading : " + (current)/(fileLength/100) + "%");
				System.out.print('\r');

				if(current == fileLength){
					System.out.print('\n');
				}

				// ?????????????????????????????? ??? ??????????????????????????????????????????.
			}while (current != fileLength);

			bos.close();

			System.out.println("        File " + fileName + " Successfully Downloaded!" );
			dout.writeInt(0);						//writeInt is used to reset if any bytes are present in the buffer after the file transfer
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}

}
