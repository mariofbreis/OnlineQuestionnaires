# Online Questionnaires
 Project for the Computer Networks course

The goal of this project was to develop an application for students to complete evaluation questionnaires online.

For this purpose, two server applications were considered: one unique centralized Evaluation Contact Point (ECP), and several instances of a Topic Evaluation Server (TES), possibly running on different machines.

The ECP, whose URL is well-known, is contacted first by the student, and provides a list of evaluation topics.

For each topic in the above referred list, there is a TES server instance that should be contacted next by the student application to obtain the corresponding questionnaire file (for instance in PDF format).

When explicitly requesting a questionnaire to the ECP, the student application specifies the topic (Tnn) on which to be evaluated. As a reply the ECP informs the student application of the IP address and port of the topic evaluation server (TES) to be contacted to ask for the questionnaire.

The appointed TES is then automatically contacted by the student application, which provides the student identification (SID) – a 5 digit number – and the selected questionnaire topic number Tnn. When the TES is contacted by the student, a unique transaction identifier string (QID) – with up to 24 characters – is assigned to this request of this student, and the TES transfers the selected questionnaire file, specifying a deadline for receiving the answer. The questionnaire file is stored into a local directory.

The topic numbers, Tnn, should follow the format “Tnn”, with nn being a number between 1 and 99. The directory where the ECP runs should contain a text file, “topics.txt”, where each line contains (separated by spaces):
- a string with the topic name.
- IP address of the TES containing the questionnaires for this topic.
- Port number of the TES containing the questionnaires for this topic.

All topics for this project must be related to the “Computer Networks”.

Each questionnaire is stored into a single file named “TnnQFxxx.pdf”, where xxx refers to the questionnaire number for a given topic Tnn.

Each questionnaire should be composed by of a set of 5 questions, including text and possibly also images. Following each question are 4 answer options (denoted: A, B, C and D), of which one and only one is correct.

The TES will also store a text file with the correct answers (one per line). The questionnaire answers file should have a name like “TnnQFxxxA.txt”, where xxx refers to the questionnaire number for a given topic Tnn.

Once the student has finished answering the questionnaire he submits the answers to the TES and, as a reply, the score is returned. The TES also informs the ECP of the results of the student in this questionnaire. The ECP keeps a record of the student results.

For the implementation, the application layer protocols operate according to the client-server paradigm, using the transport layer services made available by the socket interface, using the TCP and UDP protocols.

The ECP accepts student requests and communicates with TES servers using UDP. The student requests the selected questionnaire from the TES after establishing a TCP connection. The questionnaire answers are also sent back by students to TES servers using TCP connections.
