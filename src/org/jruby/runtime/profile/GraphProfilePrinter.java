/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.runtime.profile;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class GraphProfilePrinter extends AbstractProfilePrinter {
    public static MethodData currentData;
    private ProfileData profileData;

    public GraphProfilePrinter(IProfileData iProfileData) {
        profileData = (ProfileData) iProfileData;
    }

    public void printProfile(PrintStream out) {
        profileData.getTopInvocation().setDuration(profileData.totalTime());
        
        out.printf("\nTotal time: %s\n\n", nanoString(profileData.totalTime()));

        out.println(" %total   %self    total        self    children                 calls  name");

        Map<Integer, MethodData> methods = profileData.methodData();
        MethodData[] sortedMethods = methods.values().toArray(new MethodData[0]);
        Arrays.sort(sortedMethods, new Comparator<MethodData>() {

            public int compare(MethodData md1, MethodData md2) {
                long time1 = md1.totalTime();
                long time2 = md2.totalTime();
                return time1 == time2 ? 0 : (time1 < time2 ? 1 : -1);
            }
        });

        for (MethodData data : sortedMethods) {
            GraphProfilePrinter.currentData = data;
            if (!isProfilerInvocation(data.invocations.get(0))) {
                    
                out.println("---------------------------------------------------------------------------------------------------------");
                int serial = data.serialNumber;
                
                int[] parentSerialsInts = data.parents();
                Integer[] parentSerials = new Integer[parentSerialsInts.length];
                for (int i = 0; i < parentSerialsInts.length; i++) {
                    parentSerials[i] = parentSerialsInts[i];
                }
                
                Arrays.sort(parentSerials, new Comparator<Integer>() {
                    public int compare(Integer parent1, Integer parent2) {
                        long time1 = GraphProfilePrinter.currentData.rootInvocationsFromParent(parent1).totalTime();
                        long time2 = GraphProfilePrinter.currentData.rootInvocationsFromParent(parent2).totalTime();
                        return time1 == time2 ? 0 : (time1 < time2 ? -1 : 1);
                    }
                });
                if (parentSerials.length > 0) {
                    for (int parentSerial : parentSerials) {
                        String callerName = methodName(parentSerial);
                        InvocationSet invs = data.rootInvocationsFromParent(parentSerial);
                        out.print("              ");
                        pad(out, 10, nanoString(invs.totalTime()));
                        out.print("  ");
                        pad(out, 10, nanoString(invs.selfTime()));
                        out.print("  ");
                        pad(out, 10, nanoString(invs.childTime()));
                        out.print("  ");
                        pad(out, 20, Integer.toString(data.invocationsFromParent(parentSerial).totalCalls()) + "/" + Integer.toString(data.totalCalls()));
                        out.print("  ");
                        out.print(callerName);
                        out.println("");
                    }
                }
                
                String displayName = methodName(serial);
                pad(out, 4, Long.toString(data.totalTime() * 100 / profileData.totalTime()));
                out.print("%  ");
                pad(out, 4, Long.toString(data.selfTime() * 100 / profileData.totalTime()));
                out.print("%  ");
                pad(out, 10, nanoString(data.totalTime()));
                out.print("  ");
                pad(out, 10, nanoString(data.selfTime()));
                out.print("  ");
                pad(out, 10, nanoString(data.childTime()));
                out.print("  ");
                pad(out, 20, Integer.toString(data.totalCalls()));
                out.print("  ");
                out.print(displayName);
                out.println("");
                
                int[] childSerialsInts = data.children();
                Integer[] childSerials = new Integer[childSerialsInts.length];
                for (int i = 0; i < childSerialsInts.length; i++) {
                    childSerials[i] = childSerialsInts[i];
                }
                
                Arrays.sort(childSerials, new Comparator<Integer>() {
                    public int compare(Integer child1, Integer child2) {
                        long time1 = GraphProfilePrinter.currentData.rootInvocationsOfChild(child1).totalTime();
                        long time2 = GraphProfilePrinter.currentData.rootInvocationsOfChild(child2).totalTime();
                        return time1 == time2 ? 0 : (time1 < time2 ? 1 : -1);
                    }
                });
                
                if (childSerials.length > 0) {
                    for (int childSerial : childSerials) {
                        String callerName = methodName(childSerial);
                        InvocationSet invs = data.rootInvocationsOfChild(childSerial);
                        out.print("              ");
                        pad(out, 10, nanoString(invs.totalTime()));
                        out.print("  ");
                        pad(out, 10, nanoString(invs.selfTime()));
                        out.print("  ");
                        pad(out, 10, nanoString(invs.childTime()));
                        out.print("  ");
                        pad(out, 20, Integer.toString(data.invocationsOfChild(childSerial).totalCalls()) + "/" + Integer.toString(methods.get(childSerial).totalCalls()));
                        out.print("  ");
                        out.print(callerName);
                        out.println("");
                        
                    }
                }
            }
        }
    }
}