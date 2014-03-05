package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferedRandomAccessFile extends RandomAccessFile {

    private static final int BUF_SIZE = 1024;
    
    byte buffer[];
    int buf_end = 0;
    int buf_pos = 0;
    long real_pos = 0;
    
    public BufferedRandomAccessFile(String filename, String mode) throws IOException{
        super(filename, mode);
        invalidate();
        buffer = new byte[BUF_SIZE];
    }

    public final int read() throws IOException {
        if (buf_pos >= buf_end) {
            if (fillBuffer() < 0)
                return -1;
        }
        if (buf_end == 0) {
            return -1;
        } else {
            return buffer[buf_pos++];
        }
    }

    private int fillBuffer() throws IOException {
        int n = super.read(buffer, 0, BUF_SIZE);
        if (n >= 0) {
            real_pos += n;
            buf_end = n;
            buf_pos = 0;
        }
        return n;
    }

    private void invalidate() throws IOException {
        buf_end = 0;
        buf_pos = 0;
        real_pos = super.getFilePointer();
    }

    public int read(byte b[], int off, int len) throws IOException {
        int leftover = buf_end - buf_pos;
        if (len <= leftover) {
            System.arraycopy(buffer, buf_pos, b, off, len);
            buf_pos += len;
            return len;
        }
        for (int i = 0; i < len; i++) {
            int c = this.read();
            if (c != -1)
                b[off + i] = (byte) c;
            else {
                return i;
            }
        }
        return len;
    }

    public long getFilePointer() throws IOException {
        long l = real_pos;
        return (l - buf_end + buf_pos);
    }

    public void seek(long pos) throws IOException {
        int n = (int) (real_pos - pos);
        if (n >= 0 && n <= buf_end) {
            buf_pos = buf_end - n;
        } else {
            super.seek(pos);
            invalidate();
        }
    }
    
    /**
     * return a next line in String 
     */
    public final String getNextLine() throws IOException {
     String str = null;
     if(buf_end-buf_pos <= 0) {
        if(fillBuffer() < 0) {
                  throw new IOException("error in filling buffer!");
        }
     }
     int lineend = -1;
     for(int i = buf_pos; i < buf_end; i++) {
          if(buffer[i] == '\n') {
           lineend = i;
            break;
            }
     }
     if(lineend < 0) {
          StringBuffer input = new StringBuffer(256);
          int c;
               while (((c = read()) != -1) && (c != '\n')) {
                   input.append((char)c);
          }
          if ((c == -1) && (input.length() == 0)) {
            return null;
          }
          return input.toString();
     }
     if(lineend > 0 && buffer[lineend-1] == '\r')
          str = new String(buffer, 0, buf_pos, lineend - buf_pos -1);
     else str = new String(buffer, 0, buf_pos, lineend - buf_pos);
     buf_pos = lineend +1;
     return str;
     }

}
