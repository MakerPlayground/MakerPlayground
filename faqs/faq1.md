## What if multiple transitions become valid simultaneously?

ALLI/O Diagram language specification doesn't specify which path of the diagram to proceed when both transitions are valid simultaneously. Therefore, the behavior will depend on specific code generation implementation. The rationale is that if both transitions are valid simultaneously, proceeding to any path of the graph is valid.

In most cases this behavior is acceptable; however, if the developer wants the system to react differently when both Transitions are valid, he/she can add another Transition, e.g., Cont1, Cont2 => Cont1 && !Cont2, Cont2 && !Cont1, Cont1 && Cont2.

<img src="../images/multiple_transitions.png" width=100%>