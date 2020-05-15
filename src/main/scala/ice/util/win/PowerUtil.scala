package ice.util.win

import com.sun.jna.platform.win32._
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import com.sun.jna.{Library, Native}

object PowerUtil {

  private trait PowerProf extends Library {
    def SetSuspendState(hibernate: Boolean, force: Boolean, wakeupEventsDisabled: Boolean): Boolean
  }

  private val PowerProf: PowerProf = Native.loadLibrary("powrprof", classOf[PowerProf], W32APIOptions.DEFAULT_OPTIONS)

  def suspend(): Unit = PowerProf.SetSuspendState(hibernate = false, force = false, wakeupEventsDisabled = false)

  def hibernate(): Unit = PowerProf.SetSuspendState(hibernate = true, force = false, wakeupEventsDisabled = false)

  private def setShutdownPrivileges(): Unit = {
    val token = new WinNT.HANDLEByReference
    Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess, WinNT.TOKEN_ADJUST_PRIVILEGES, token)
    val luid = new WinNT.LUID
    Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_SHUTDOWN_NAME, luid)
    val tokenPrivileges = new WinNT.TOKEN_PRIVILEGES(1)
    tokenPrivileges.Privileges(0) = new WinNT.LUID_AND_ATTRIBUTES(luid, new WinDef.DWORD(WinNT.SE_PRIVILEGE_ENABLED))
    Advapi32.INSTANCE.AdjustTokenPrivileges(token.getValue, false, tokenPrivileges, 0, null, new IntByReference(0))
  }

  private def exitWindows(flag: Int, forceIfHung: Boolean, force: Boolean): Unit = {
    setShutdownPrivileges()

    val flags = flag | (if (forceIfHung) WinUser.EWX_FORCEIFHUNG else 0) | (if (force) WinUser.EWX_FORCE else 0)

    val ret = User32.INSTANCE.ExitWindowsEx(new WinDef.UINT(flags), new WinDef.DWORD(0)).booleanValue

    if (!ret) {
      val err = Kernel32.INSTANCE.GetLastError
      throw new RuntimeException("ExitWindowsEx error " + err + " " + Kernel32Util.formatMessageFromLastErrorCode(err))
    }
  }

  def logoff(forceIfHung: Boolean = false, force: Boolean = false): Unit = exitWindows(WinUser.EWX_LOGOFF, forceIfHung, force)

  def poweroff(forceIfHung: Boolean = false, force: Boolean = false): Unit = exitWindows(WinUser.EWX_POWEROFF, forceIfHung, force)

  def reboot(forceIfHung: Boolean = false, force: Boolean = false): Unit = exitWindows(WinUser.EWX_REBOOT, forceIfHung, force)

  def main(args: Array[String]): Unit = suspend()
}

