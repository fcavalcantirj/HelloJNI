package hellojni.example.com.hellojni;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.chilkatsoft.CkSsh;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {

    private static final String TAG = "Chilkat";

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Important: It is helpful to send the contents of the
        //  ssh.LastErrorText property when requesting support.

        CkSsh ssh = new CkSsh();

        //  Any string automatically begins a fully-functional 30-day trial.
        boolean success;
        success = ssh.UnlockComponent("30-day trial");
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Connect to an SSH server:
        String hostname;
        int port;

        //  Hostname may be an IP address or hostname:
        hostname = "192.168.1.131";
        port = (int) 22;

        success = ssh.Connect(hostname,port);
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Wait a max of 5 seconds when reading responses..
        ssh.put_IdleTimeoutMs(5000);

        //  Authenticate using login/password:
        success = ssh.AuthenticatePw("pi","raspberry");
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Open a session channel.  (It is possible to have multiple
        //  session channels open simultaneously.)
        int channelNum;
        channelNum = (int) ssh.OpenSessionChannel();
        if (channelNum < 0) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Some SSH servers require a pseudo-terminal
        //  If so, include the call to SendReqPty.  If not, then
        //  comment out the call to SendReqPty.
        //  Note: The 2nd argument of SendReqPty is the terminal type,
        //  which should be something like "xterm", "vt100", "dumb", etc.
        //  A "dumb" terminal is one that cannot process escape sequences.
        //  Smart terminals, such as "xterm", "vt100", etc. process
        //  escape sequences.  If you select a type of smart terminal,
        //  your application will receive these escape sequences
        //  included in the command's output.  Use "dumb" if you do not
        //  want to receive escape sequences.  (Assuming your SSH
        //  server recognizes "dumb" as a standard dumb terminal.)
        String termType;
        termType = "dumb";
        int widthInChars;
        widthInChars = (int) 120;
        int heightInChars;
        heightInChars = (int) 40;
        //  Use 0 for pixWidth and pixHeight when the dimensions
        //  are set in number-of-chars.
        int pixWidth;
        pixWidth = (int) 0;
        int pixHeight;
        pixHeight = (int) 0;
        success = ssh.SendReqPty(channelNum,termType,widthInChars,heightInChars,pixWidth,pixHeight);
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Start a shell on the channel:
        success = ssh.SendReqShell(channelNum);
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Start a command in the remote shell.  This example
        //  will send a "ls" command to retrieve the directory listing.
        success = ssh.ChannelSendString(channelNum,"ls\r\n","ansi");
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Send an EOF.  This tells the server that no more data will
        //  be sent on this channel.  The channel remains open, and
        //  the SSH client may still receive output on this channel.
        success = ssh.ChannelSendEof(channelNum);
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Read whatever output may already be available on the
        //  SSH connection.  ChannelReadAndPoll returns the number of bytes
        //  that are available in the channel's internal buffer that
        //  are ready to be "picked up" by calling GetReceivedText
        //  or GetReceivedData.
        //  A return value of -1 indicates failure.
        //  A return value of -2 indicates a failure via timeout.

        //  The ChannelReadAndPoll method waits
        //  for data to arrive on the connection usingi the IdleTimeoutMs
        //  property setting.  Once the first data arrives, it continues
        //  reading but instead uses the pollTimeoutMs passed in the 2nd argument:
        //  A return value of -2 indicates a timeout where no data is received.
        int n;
        int pollTimeoutMs;
        pollTimeoutMs = (int) 2000;
        n = (int) ssh.ChannelReadAndPoll(channelNum,pollTimeoutMs);
        if (n < 0) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Close the channel:
        success = ssh.ChannelSendClose(channelNum);
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Perhaps we did not receive all of the commands output.
        //  To make sure,  call ChannelReceiveToClose to accumulate any remaining
        //  output until the server's corresponding "channel close" is received.
        success = ssh.ChannelReceiveToClose(channelNum);
        if (success != true) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Let's pickup the accumulated output of the command:
        String cmdOutput;
        cmdOutput = ssh.getReceivedText(channelNum,"ansi");
        if (cmdOutput == null ) {
            Log.i(TAG, ssh.lastErrorText());
            return;
        }

        //  Display the remote shell's command output:
        Log.i(TAG, cmdOutput);

        TextView  tv = new TextView(this);
        tv.setText( cmdOutput );
        setContentView(tv);

        //  Disconnect
        ssh.Disconnect();
    }

    static {
        // Important: Make sure the name passed to loadLibrary matches the shared library
        // found in your project's libs/armeabi directory.
        //  for "libchilkat.so", pass "chilkat" to loadLibrary
        //  for "libchilkatemail.so", pass "chilkatemail" to loadLibrary
        //  etc.
        //
        System.loadLibrary("chilkatssh");

        // Note: If the incorrect library name is passed to System.loadLibrary,
        // then you will see the following error message at application startup:
        //"The application <your-application-name> has stopped unexpectedly. Please try again."
    }
}
