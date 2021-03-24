# Options
1. Command pattern
2. One class derived from equipment with variable arguments for its functions 
3. One class derived from equipment, but the functions take 0 parameters and we use dynamic annotation to pass what parameters are needed from the class to the VDM
4. Implement an interface, a Java class derived from that and a VDM class as well. The main could use the Java one and the RemoteControl could use the VDM one.

# What is needed for everything
1. Casting from Java types to VDM types
2. An annotation system that generates the Java class that maps to VDM (except case 4). Could also generate the body of the VDM class which could be expanded later