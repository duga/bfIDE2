package tpsa;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import tpsa.exceptions.BFException;
import tpsa.exceptions.BadCallException;
import tpsa.exceptions.MalformedCodeException;
import tpsa.exceptions.OutOfMemoryPointer;
import tpsa.streams.JTextPaneInputStream;
import tpsa.streams.JTextPaneOutputStream;

/**
 * Brainfuck is an esoteric programming language noted for its extreme minimalism.
 * It is a Turing tarpit, designed to challenge and amuse programmers,
 * and was not made to be suitable for practical use. It was created in 1993 by Urban Müller.
 * The name of the language is generally not capitalized except at the start of a sentence,
 * although it is a proper noun.
 * 
 * Język ma 8 mnemoników (patrz mnemoniki assemblera)
 * 
 * >	zwiększa wskaźnik o 1	                                 ++p
 * <	zmniejsza wskaźnik o 1	                                 --p
 * +	zwiększa o 1 w bieżącej pozycji	                         ++(*p)
 * -	zmniejsza o 1 w bieżącej pozycji	                     --(*p)
 * .	wyświetla znak w bieżącej pozycji (ASCII)	             putchar(*p)
 * ,	pobiera znak i wstawia go w bieżącej pozycji (ASCII)	 *p=getchar()
 * [	skacze bezpośrednio za odpowiadający mu ], jeśli w 
 *      bieżącej pozycji znajduje się 0	                         while(*p){
 * ]	skacze do odpowiadającego mu [	                         }
 * 
 */

/**
 * Klasa główna maszyny BF. Jest ona odpowiedzialna, za stworzenie VM dla BF i
 * interpretowanie kodu BF.
 * 
 * @author Duga Eye
 */
public class BFMachine implements Runnable {

	public static int defaultMemorySize = 0x10000;
	/**
	 * Kod, dla maszyny wirtualnej BF
	 */
	private Runnable breakpointHandler;
	private byte[] code;
	private int codePointer = 0;
	private String error;
	private InputStreamReader iStreamReader = null;
	private InputStream iS = null;
	private Object lock = new Object();
	private AtomicBoolean machineWorking = new AtomicBoolean(false);
	private byte[] memory;
	private int memoryPointer = 0;
	private OutputStreamWriter oStream = null;
	private AtomicBoolean paused = new AtomicBoolean(false);
	private Runnable postHandler;
	private Stack<Integer> stackExecution = new Stack<Integer>();

	public void reset() {
		memoryPointer = memory.length / 2;
		Arrays.fill(memory, (byte) 0);
		try {
			iS.reset();
		} catch (IOException excp) {

		}
		codePointer = 0;
	}

	private Runnable stepHandler;

	private AtomicBoolean stepping = new AtomicBoolean(false);

	public BFMachine(JTextPaneInputStream is, JTextPaneOutputStream os,
			String code, int sizeOfMemory) {
		this.code = code.getBytes();
		memory = new byte[sizeOfMemory];
		memoryPointer = sizeOfMemory / 2;
		iS = is;
		oStream = new OutputStreamWriter(os);
		iStreamReader = new InputStreamReader(is);
	}

	/**
	 * Domyślnie program startuje z wskaźnikiem pamięci w środku pamięci
	 * operacynej
	 * 
	 * @param code
	 *            Kod, który wykonywany jest przez maszynę BF
	 */
	public BFMachine(String code) {
		this(code, defaultMemorySize);
	}

	/**
	 * Domyślnie program startuje z wskaźnikiem pamięci w środku pamięci
	 * operacynej
	 * 
	 * @param code
	 *            Kod, który wykonywany jest przez maszynę BF
	 * @param sizeOfMemory
	 *            rozmiar pamięci operacyjnej maszyny BF
	 */
	public BFMachine(String code, int sizeOfMemory) {
		this.code = code.getBytes();
		memory = new byte[sizeOfMemory];
		memoryPointer = sizeOfMemory / 2;
		error = "";
	}

	public Runnable getBreakpointHandler() {
		return breakpointHandler;
	}

	public String getError() {
		if (error == null)
			return new String();
		return new String(error);
	}

	public byte[] getMemory() {
		return memory;
	}

	public int getMemoryPointer() {
		return memoryPointer;
	}

	public int getPosition() {
		return codePointer;
	}

	public Runnable getStepHandler() {
		return stepHandler;
	}

	public boolean getStepping() {
		return stepping.get();
	}

	public boolean isWorking() {
		return machineWorking.get();
	}

	/**
	 * Zatrzymuje pracę maszynę BF
	 */
	public void pause() {
		paused.set(true);
	}

	/**
	 * Wznawia pracę maszyny BF
	 */
	public void resume() {
		paused.set(false);
		synchronized (lock) {
			lock.notify();
		}
	}

	/**
	 * Przewija wskaźnik kodu na pozycję o 1 dalej niż jest koniec
	 * odpowiadającemu mu znakowi końca pętli
	 * 
	 * @throws BadCallException
	 *             W miejscu wskaźnika BF nie ma znaku początku pętli
	 * @throws MalformedCodeException
	 *             Kod podany dla maszyny BF jest niepoprawny
	 */
	private void rewindTheLoopToTheEnd() throws BadCallException,
			MalformedCodeException {
		if (code[codePointer] != '[')
			throw new BadCallException("Nie ma '[' w miejscu startu",
					codePointer);
		// System.err.println("Wchodzę w pozycji: " + codePointer);
		int stack = 1;
		codePointer++;

		while (stack > 0) {
			while (code[codePointer] != '[' && code[codePointer] != ']') {
				// System.err.println("Pozycja: " + codePointer + ", char: " +
				// code[codePointer]);
				if (++codePointer >= code.length)
					throw new MalformedCodeException(
							"Nie wiem jakim cudem, ale pointer wyszedł poza kod",
							codePointer);
			}
			// System.err.println("Pozycja: " + codePointer + ", char: " +
			// code[codePointer]);
			switch (code[codePointer]) {
			case (byte) 0xcc:
				/*
				 * if (breakpointHandled.get() == false) return; else {
				 * breakpointHandled.set(true); }
				 */
				break;
			case '[':
				++stack;
				break;
			case ']':
				--stack;
				break;
			}
			codePointer++;
		}
		// System.err.println("Znak wyjścia: " + codePointer + ", char: " +
		// code[codePointer]);

	}

	@Override
	public void run() {
		memoryPointer = memory.length / 2;
		codePointer = 0;

		BFInterpreter.lg.info("Machine starting");

		BFInterpreter.lg.finest("Code" + new String(code));

		machineWorking.set(true);

		try {

			while (codePointer < code.length && machineWorking.get()) {

				if (paused.get() || stepping.get()) {

					if (stepHandler != null && stepping.get()) {
						BFInterpreter.lg.fine("Stepping item");
						System.err.println("Joł");
						stepHandler.run();
					}

					synchronized (lock) {
						lock.wait();
					}
				}

				// BFInterpreter.lg.finest("Executing code at " + codePointer);

				switch (code[codePointer]) {
				case 'b':
					if (breakpointHandler != null) {
						breakpointHandler.run();
						synchronized (lock) {
							lock.wait();
						}

					} else
						return;
					break;
				case '>':
					if (++memoryPointer >= memory.length)
						throw new OutOfMemoryPointer(
								"Pointer wyszedł poza pamięć", codePointer);
					break;
				case '<':
					if (--memoryPointer < 0)
						throw new OutOfMemoryPointer(
								"Pointer wyszedł poza pamięć", codePointer);
					break;
				case '+':
					++memory[memoryPointer];
					break;
				case '-':
					--memory[memoryPointer];
					break;
				case '.':
					BFInterpreter.lg.finest("Priting char");
					if (oStream == null) {
						System.out.print(memory[memoryPointer]);
					} else {
						oStream.write(memory[memoryPointer]);
					}
					break;
				case ',':
					BFInterpreter.lg.fine("Reading char from input");
					int c = (iStreamReader == null) ? System.in.read() : iStreamReader
							.read();
					BFInterpreter.lg.fine("Read input code " + c);
					memory[memoryPointer] = (byte) c;
					BFInterpreter.lg.fine("Read char");
					break;
				case '[':
					// System.err.println("Wartość w komórce: " +
					// (int)memory[memoryPointer]);
					if (memory[memoryPointer] > 0)
						stackExecution.push(codePointer);
					else {
						rewindTheLoopToTheEnd();
						continue;
					}
					break;
				case ']':
					int pos = stackExecution.pop();
					codePointer = pos;
					continue;
				}
				codePointer++;
				Thread.yield();
			}
			if (oStream != null)
				oStream.flush();
			// oStream.close();
		} catch (BFException excp) {
			String message = excp.getMessage();
			if (message == null)
				message = new String("Exception");
			setError(excp.getClass().getName() + ": " + message + ", offset: "
					+ excp.getOffset());

		} catch (EmptyStackException excp) {
			setError(excp.getClass().getName()
					+ "Stary, klamerki się nie zgadzają");
		} catch (IOException excp) {
			setError("No nic, IOException..., cokolwiek by to nie znaczyło");
		} catch (InterruptedException excp) {
			BFInterpreter.lg.severe(BFInterpreter.ErrorsFormatter(excp));
		}

		if (postHandler != null && machineWorking.get() == true)
			postHandler.run();

		machineWorking.set(false);

		BFInterpreter.lg.finest("Machine has ended");
	}

	public void setBreakpointHandler(Runnable breakpointHandler) {
		this.breakpointHandler = breakpointHandler;
	}

	public void setCode(String code) {
		this.code = code.getBytes();
	}

	public void setError(String error) {
		this.error = new String(error);
	}

	public void setFinishHandler(Runnable runnable) {
		this.postHandler = runnable;

	}

	public void setStepHandler(Runnable stepHandler) {
		this.stepHandler = stepHandler;
	}

	public void setStepping(boolean b) {
		this.stepping.set(b);
	}

	/**
	 * Definitywnie kończy pracę maszyny (nie da się wznowić)
	 * 
	 * @see pause
	 */
	public void stop() {
		// definitywnie kończy pracę, nie da się wznowić
		machineWorking.set(false);
	}

	public byte[] getCode() {
		return code;
	}

	public void setCode(byte[] code) {
		this.code = code;
	}

}
