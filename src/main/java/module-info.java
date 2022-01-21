/**
 * <b>Shipping Port Simulation</b>
 * <br>
 * Assignment 1 &amp; 2 (combined) for
 * <a href="https://my.uq.edu.au/programs-courses/course.html?course_code=CSSE2002">CSSE2002</a>,
 * Semester 2 2021
 * <br><br>
 * <b>Acknowledgment</b>:
 * <ul>
 *     <li>
 *         The majority of the JavaDoc in portsim was taken from the provided
 *         <a href="https://csse2002.uqcloud.net/assignment/2/index.html">specification</a>.
 *     </li>
 *     <li>
 *         All code marked with {@code @given} was provided by course staff.
 *     </li>
 * </ul>
 *
 * @author Jed Willick
 */
module portsim.main {
    requires transitive javafx.graphics;
    requires javafx.controls;
    exports portsim;
    exports portsim.cargo;
    exports portsim.display;
    exports portsim.evaluators;
    exports portsim.movement;
    exports portsim.port;
    exports portsim.ship;
    exports portsim.util;
}