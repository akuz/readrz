package com.readrz.topicalc;

import me.akuz.core.Index;

import com.readrz.data.index.KeysIndex;
import com.readrz.math.topicmodels.LDABuildTopicsTree;
import com.readrz.math.topicmodels.LDABuildTopicsTreeNode;
import com.readrz.search.QueryParser;

public final class BuildTopicsTree {
	
	public static final LDABuildTopicsTree buildReadrzTopicTree(
			double backgroundTopicPriorProb,
			int transientTopicsCount,
			double transientTopicPriorProb,
			double unnecessaryTopicPriorProb,
			double priorityTopicPriorProb,
			double priorityStemsMassFraction,
			QueryParser queryParser,
			KeysIndex readOnlyKeysIndex,
			Index<String> readOnlyStemsIndex) {

		System.out.println("Setting up topics...");
		LDABuildTopicsTree buildTopicsTree 
			= new LDABuildTopicsTree(
				queryParser, 
				readOnlyKeysIndex, 
				readOnlyStemsIndex, 
				priorityStemsMassFraction);
		
		// get root node
		LDABuildTopicsTreeNode rootNode = buildTopicsTree
				.getRootNode();

		{ // de-noise ========>
			
			rootNode
				.addChildNode(
						backgroundTopicPriorProb, 
						"background", 
						"Background");
			
			for (int i=0; i<transientTopicsCount; i++){
				rootNode
					.addChildNode(transientTopicPriorProb, 
							"transient_" + i, 
							"Transient " + i)
					.isTransient(true);
			}
			
			rootNode
				.addChildNode(unnecessaryTopicPriorProb, 
						"copyright", 
						"Copyright")
				.addPriorityWords(
						"associated press copyright copy rights material redistribution " +
						"publishing distribute permission reserved prohibited ")
				.addExcludedWords(
						"human ben bernanke federal economy")
				.isTransient(true);
			
			rootNode
				.addChildNode(unnecessaryTopicPriorProb,
						"follow_us", 
						"Follow Us")
				.addPriorityWords(
						"follow twitter post share recommend facebook ")
				.isTransient(true);
		}
		
		{ // finance ========>
			
			LDABuildTopicsTreeNode financeNode 
				= rootNode
					.addChildNode(0, 
							"finance", "Finance")
					.addPriorityWords(
							"boom gains sharp fallen growing growth surged " +
							"rose fell crash loss dropped jump price ")
					.isGroup(true);

			{ // economy -------->
				
				LDABuildTopicsTreeNode economyNode 
					= financeNode
						.addChildNode(0, 
								"economy", "Economy")
						.addPriorityWords(
								"economy economist national statistics ")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				economyNode
					.addChildNode(priorityTopicPriorProb, 
							"market", "Market")
					.addPriorityWords(
							"market boost data global recovery " +
							"recession price emerging interest rate ")
					.isGroup(true);
				
				economyNode
					.addChildNode(priorityTopicPriorProb, 
							"gov_policy", "Gov. Policy")
					.addPriorityWords(
							"economy fed central bank policy interest rate invervention " +
							"quantitative easing tax cuts spending budget treasury " +
							"national statistics policy bond yield ")
					.isGroup(true);

				economyNode
					.addChildNode(priorityTopicPriorProb, 
							"production", "Production")
					.addPriorityWords(
							"production output period goods cost factors " +
							"efficiency total increase decrease ")
					.isGroup(true);
				
				economyNode
					.addChildNode(priorityTopicPriorProb, 
							"real_estate", "Real Estate")
					.addPriorityWords(
							"real estate home housing property developer building " +
							"rent rental built buy sold selling buyers mortgage " +
							"owners residential apartment mansion land homeowners " +
							"lease bedroom condo foreclosure residents " +
							"renovation builders landlords")
					.isGroup(true);

				economyNode
					.addChildNode(priorityTopicPriorProb, 
							"workers", "Workers")
					.addPriorityWords(
							"workers pay strike protest contracts union " +
							"factory labor labour stuff minimum wage ")
					.isGroup(true);
				
				economyNode
					.addChildNode(priorityTopicPriorProb, 
							"jobs", "Jobs")
					.addPriorityWords(
							"jobs employment unemployment jobless ")
					.isGroup(true);
			}

			{ // sectors -------->
				
				LDABuildTopicsTreeNode sectorsNode 
					= financeNode
						.addChildNode(0, 
								"sectors", "Sectors")
						.isGroup(true)
						.isGroupTaxonomy(true);

				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"banking", "Banking")
					.addPriorityWords(
							"bank investment central credit debt loan lender " +
							"banker global trading trader services deposit ")
					.isGroup(true);
			
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"capital", "Capital")
					.addPriorityWords(
							"capital hedge fund asset management raise investing " +
							"investment private venture vc partner buyout equity ")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"energy", "Energy")
					.addPriorityWords(
							"oil gas crude electricity output production opec global " +
							"trend events producers structural demand supply factors ")
					.isGroup(true);
			}

			{ // stocks -------->
				
				LDABuildTopicsTreeNode stocksNode 
					= financeNode
						.addChildNode(0, 
								"stocks", "Stocks")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				stocksNode
					.addChildNode(priorityTopicPriorProb, 
							"stock_market", "Stock Market")
					.addPriorityWords(
							"stock market investors index portfolio dividend " +
							"stimulus rally record equity performance equity " +
							"traded european american asian ")
					.isGroup(true);

				stocksNode
					.addChildNode(priorityTopicPriorProb, 
							"ratings", "Ratings")
					.addPriorityWords(
							"stock rating reiterated reaffirmed target analysts neutral " +
							"overweight underweight upgraded downgraded hold sell buy " +
							"lowered outperform underperform given restated received ")
					.addExcludedWords(
							"interest vouchers england craigslist seattle ")
					.isGroup(true);

				stocksNode
					.addChildNode(priorityTopicPriorProb, 
							"results", "Results")
					.addPriorityWords(
							"results estimates profit sales earnings revenue " +
							"million billion expectations forecast analysts beat " +
							"operating q1 q2 q3 q4 quarter")
					.isGroup(true);
				
				stocksNode
					.addChildNode(priorityTopicPriorProb, 
							"conf_calls", "Conf. Calls")
					.addPriorityWords(
							"results conference call transcript earnings " +
							"sales profit expected forecast estimates " +
							"q1 q2 q3 q4 quarter company group holdings")
					.isGroup(true);
			}

			{ // events -------->
				
				LDABuildTopicsTreeNode eventsNode 
					= financeNode
						.addChildNode(0, 
								"events", "Events")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				eventsNode
					.addChildNode(priorityTopicPriorProb, 
							"distress", "Distress")
					.addPriorityWords(
							"bankruptcy bankrupt distress protection " +
							"debt situation temporary crisis dispute " +
							"shortfall cash deficit problems ")
					.isGroup(true);
				
				eventsNode
					.addChildNode(priorityTopicPriorProb, 
							"fin_crime", "Fin. Crime")
					.addPriorityWords(
							"rogue trader bank fund capital million regulators insider " +
							"trading manipulate federal sac charges against irs sec" +
							"case indictment fraud criminal allegations")
					.isGroup(true);
			}
		}
		
		{ // business ========>
			
			LDABuildTopicsTreeNode businessNode 
				= rootNode
					.addChildNode(0, 
							"business", "Business")
					.addPriorityWords(
							"business company firm")
					.isGroup(true);
			
			{ // events -------->
				
				LDABuildTopicsTreeNode eventsNode 
					= businessNode
						.addChildNode(0, 
								"events", "Events")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				eventsNode
					.addChildNode(priorityTopicPriorProb, 
							"deals", "Deals")
					.addPriorityWords(
							"deal company group target million billion bid buy bought acquired " +
							"plan largest agreement shareholders vote approved agreed ")
					.isGroup(true);
	
				eventsNode
					.addChildNode(priorityTopicPriorProb, 
							"management", "Management")
					.addPriorityWords(
							"management manager chief executive head ceo director board team " +
							"president chairman advisor named former leaving top replace " +
							"appointed role hired retired resigned served joined ")
					.isGroup(true);
			}			
			
			
			{ // sectors -------->
				
				LDABuildTopicsTreeNode sectorsNode 
					= businessNode
						.addChildNode(0, 
								"sectors", "Sectors")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				LDABuildTopicsTreeNode technologyNode 
					= sectorsNode
						.addChildNode(0,
								"technology", "Technology")
						.addPriorityWords(
								"technology design tech hitech")
						.isGroup(true);
				
				{ // subsectors -------->
					
					LDABuildTopicsTreeNode subsectorsNode 
						= technologyNode
							.addChildNode(0,
									"areas", "Areas")
							.isGroup(true)
							.isGroupTaxonomy(true);

					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"apps", "Apps")
						.addPriorityWords(
								"app store developer launch users update released version " +
								"download launch social video screen photo feature post " +
								"mobile devices tablet smartphone features enables beta ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"devices", "Devices")
						.addPriorityWords(
								"device phone smartphone iphone ipad tablet handset system " +
								"launch upgrade surface screen glass released operating " +
								"version unveiled technology product electonics maker ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"startups", "Startups")
						.addPriorityWords(
								"innovation technology startup business computer design " +
								"silicon valley team entrepreneurs founder product future " +
								"digital created released working stealth mode beta ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"internet", "Internet")
						.addPriorityWords(
								"social networking media internet community online website " +
								"site communication message post account profile register " +
								"members searching popular followers community ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"games", "Games")
						.addPriorityWords(
								"game gamer play video console zynga xbox kinect nintendo playstation " +
								"ps4 3ds wii massively multiplayer mobile online social hardware " +
								"graphic card developer farmville streetpass ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"hacking", "Hacking")
						.addPriorityWords(
								"hacking security developers attack cyber users flaw vulnerability " +
								"passwords fix target breach steal privacy protect bitcoin ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"software", "Software")
						.addPriorityWords(
								"software enterprise operating system cloud release update version " +
								"development program open source code designed keywords " +
								"search engine linux windows information technology ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"space", "Space")
						.addPriorityWords(
								"space astronaut aerospace satellite mission rocket onboard " +
								"international space center station iss launch unmanned cargo " +
								"transporter carrying equipment experimental mars rover ")
						.isGroup(true);
					
					subsectorsNode
						.addChildNode(priorityTopicPriorProb, 
								"cars", "Cars")
						.addPriorityWords(
								"car auto automobile automotive automaker vehicle model electric " +
								"hybrid engine battery market industry cabin navigation dashboard " +
								"production industry horsepower transmission concept chassis ")
						.isGroup(true);
				}
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"energy", "Energy")
					.addPriorityWords(
							"energy power electricity oil gas fossil fuel coal wind solar nuclear " +
							"power production plant network development policy chevron bp ")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"retail", "Retail")
					.addPriorityWords(
							"retail retail supermarket distribution mall chain food grocery " +
							"electronics brand products producer goods shampoo soap cosmetics " +
							"tobacco growth sales operating margin ")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"telecom", "Telecom")
					.addPriorityWords(
							"telecom handsets mobile phones service plan smartphone customer " +
							"operating gsm cdma 3g 4g sim card roaming licences network " +
							"devices makers wireless prices verizon vodafone ")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"pharma", "Pharma")
					.addPriorityWords(
							"pharma pharmaceuticals food drug administration fda approved " +
							"treatment treat patients clinical trial development evaluating " +
							"outcome tests efficacy deceases disorders conditions gsk ")
					.addExcludedWords(
							"trafficking cacaine cartel authorities illegal onyx dealer ")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"materials", "Materials")
					.addPriorityWords(
							"iron ore copper aluminium steel metal chemicals forest " +
							"production development factory plant tonnes utilisation " +
							"capacity mine miners operation workers ")
					.addExcludedWords(
							"investigation accused ")
					.isGroup(true);
				
			}
		}
		
		{ // science ========>
			
			LDABuildTopicsTreeNode scienceNode 
				= rootNode
					.addChildNode(0, 
							"science", "Science")
					.addPriorityWords(
							"research researcher science scientist " +
							"discovery experiment analysis laboratory lab")
					.isGroup(true);

			{ // sectors -------->
				
				LDABuildTopicsTreeNode sectorsNode 
					= scienceNode
						.addChildNode(0, 
								"sectors", "Sectors")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"natural", "Natural")
					.addPriorityWords(
							"physics physicist hadron particle collider atom " +
							"supercooled tunnel higgs neutrino proton photon boson " +
							"interstellar stellar galaxy cosmic flux waves " +
							"temperature light energy complex " + 
							"mathematics maths formula equation polynomial complexity " +
							"fractals monoid topology differential calculation " +
							"simulation dimensions algebra applied magnet " +
							"euclidian geometry logic")
					.addExcludedWords(
							"anders breivik trolls samsung iwatch trademark iii s4 " +
							"boris johnson fair ford london scotland essex theresa")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"med_bio", "Med-Bio")
					.addPriorityWords(
							"medicine drug discover testing cancer hiv brain scan " +
							"nutrition parasitic infestations therapy malignant tumors breast " +
							"immunotherapy gene therapy metastasis prognosis blood cells " +
							"biology biologist humal animal stem cells sex skin nerve " +
							"computational tumor tissue heart regenerative molecule genetic " +
							"gene mice regeneration protein acid nucleus bioengineer")
					.addExcludedWords(
							"dinosaur archaeologist ancient cartel " +
							"zetas captured leader strangler doctor hospital " +
							"star apprentice actor singer bolshoi " +
							"mammoth ")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"climate", "Climate")
					.addPriorityWords(
							"climate change environment species air pollution emissions " +
							"co2 carbon dioxide global warming temperature surface ocean data " +
							"marine ice iceberg glacier greenhouse gasses planet sustainability")
					.addExcludedWords(
							"mandela hospital birthday family celebrate")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"animals", "Animals")
					.addPriorityWords(
							"animals habitat wild wildness wilelife giraffe monkey elephant " +
							"tiger zebra rhino leopard fish cat dog chimpanzee birds whale " +
							"marine sea life zoo environment reptiles mammals mammoth panda " +
							"conservation species veterinary")
					.addExcludedWords(
							"3d printer jason 3-d")
					.isGroup(true);				
			}
		}

		{ // government ========>
			
			LDABuildTopicsTreeNode governmentNode 
				= rootNode
					.addChildNode(0, 
							"government", "Government")
					.addPriorityWords(
							"government public president prime minister " +
							"officials administration minister agency")
					.isGroup(true);
			
			{ // politics -------->
				
				LDABuildTopicsTreeNode politicsNode 
					= governmentNode
						.addChildNode(0, 
								"politics", "Politics")
						.addPriorityWords(
								"politics party coalition " +
								"liberal conservative leader ")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				politicsNode
					.addChildNode(priorityTopicPriorProb, 
							"us", "U.S.")
					.addPriorityWords(
							"senate congress white house secretary of state republican " +
							"democrat washington representatives dems reps")
					.isGroup(true);
				
				politicsNode
					.addChildNode(priorityTopicPriorProb, 
							"uk", "U.K.")
					.addPriorityWords(
							"house parlament commons lords union labour tory tories " +
							"party mp mps miliband blair cameron")
					.isGroup(true);
				
				politicsNode
					.addChildNode(priorityTopicPriorProb, 
							"world", "World")
					.addPriorityWords(
							"world un u.n united nations peace global summit conference " +
							"agreement meeting international independence referendum " +
							"nuclear atomic weapons uranium plutonium enrichment program " +
							"facility centrifuges bomb missile proliferation negotiations " +
							"disarmament elimination abolition talks conference " +
							"peace dangerous security hiroshima settlements " +
							"diplomatic discuss sanctions dispute envoy ")
					.isGroup(true);
				
				politicsNode
					.addChildNode(priorityTopicPriorProb, 
							"elections", "Elections")
					.addPriorityWords(
							"elections vote campaign poll win won vistory lost leader " +
							"results called upper lower electoral disputed ballot")
					.isGroup(true);
				
				politicsNode
					.addChildNode(priorityTopicPriorProb, 
							"local", "Local")
					.addPriorityWords(
							"mayor city campaign governor election fundraiser " +
							"signatures borough local region ")
					.isGroup(true);
			}
			
			{ // sectors -------->
				
				LDABuildTopicsTreeNode sectorsNode 
					= governmentNode
						.addChildNode(0, 
								"sectors", "Sectors")
						.isGroup(true)
						.isGroupTaxonomy(true);

				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"law", "Law")
					.addPriorityWords(
							"law lawsuit high supreme court judge justice federal " +
							"appeal ban bill inquiry civil suit commission " +
							"attorney general department committee review")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"security", "Security")
					.addPriorityWords(
							"security data surveillance nsa cyber hacking information " +
							"internet spy phone calls intelligence access privacy " +
							"personal information leaks leaker wikileaks revealed " +
							"system communications protect prism track")
					.isGroup(true);
				
				sectorsNode
					.addChildNode(priorityTopicPriorProb, 
							"military", "Military")
					.addPriorityWords(
							"military forces arms drone program funding army troops " +
							"civilians equipment tanker marine carrier " +
							"navy exercise training")
					.isGroup(true);
			}
		}
		
		{ // society ========>
			
			LDABuildTopicsTreeNode societyNode 
				= rootNode
					.addChildNode(0, 
							"society", "Society")
					.addPriorityWords(
							"society public people")
					.isGroup(true);
			
			{ // conflicts -------->
				
				LDABuildTopicsTreeNode conflictsNode 
					= societyNode
						.addChildNode(0, 
								"conflicts", "Conflicts")
						.addPriorityWords(
								"conflicts")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				conflictsNode
					.addChildNode(priorityTopicPriorProb, 
							"protests", "Protests")
					.addPriorityWords(
							"protest ousted opposition revolution islamist clashes coup " +
							"streets square police city activists crowd violence arrest " +
							"removal demand demonstration rallies democracy")
					.isGroup(true);
				
				conflictsNode
					.addChildNode(priorityTopicPriorProb, 
							"terror", "Terror")
					.addPriorityWords(
							"terror terrorist attack explode explosion bomb blast detonated " +
							"victims suspect dead deadly survive mosque muslim " +
							"islamist claim responsibility violence")
					.isGroup(true);
				
				conflictsNode
					.addChildNode(priorityTopicPriorProb, 
							"war", "War")
					.addPriorityWords(
							"war arms fight region military army interim rebel forces " +
							"troops militants blast wounded killed revolution commander " +
							"exploded jets dropped marine serious damage " +
							"fired bomb attack defence soldier")
					.isGroup(true);
			}
			
			{ // crime -------->
				
				LDABuildTopicsTreeNode crimeNode 
					= societyNode
						.addChildNode(0, 
								"crime", "Crime")
						.addPriorityWords(
								"crime criminal murderer thief robbery " +
								"theft steal arson killed victims death ")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				crimeNode
					.addChildNode(priorityTopicPriorProb, 
							"victims", "Victims")
					.addPriorityWords(
							"police woman women man men boy girl teenager missing search body " +
							"victim suspect injured beaten assulted remains discovered " +
							"abuse bullying bullied tragic ")
					.isGroup(true);
				
				crimeNode
					.addChildNode(priorityTopicPriorProb, 
							"investigation", "Investigation")
					.addPriorityWords(
							"police investigation evidence interview gather arrived captured " +
							"gun gunman shooting shot fire officers armed cops wounded leader " +
							"suspect bullets carrying cartel marijuana zetas meth " +
							"scotland yard detective ")
					.isGroup(true);
				
				crimeNode
					.addChildNode(priorityTopicPriorProb, 
							"prosecution", "Prosecution")
					.addPriorityWords(
							"trial court charges judge case prison punishment accused verdict " +
							"guilty convicted defence jury jurors procecutor procecution testify")
					.isGroup(true);
			}
			
			{ // issues -------->
				
				LDABuildTopicsTreeNode issuesNode 
					= societyNode
						.addChildNode(0, 
								"issues", "Issues")
						.addPriorityWords(
								"issues government administration reaction outrage")
						.isGroup(true)
						.isGroupTaxonomy(true);

				issuesNode
					.addChildNode(priorityTopicPriorProb,
							"education", "Education")
					.addPriorityWords(
							"education higher university school course " +
							"program children students")
					.isGroup(true);
			
				issuesNode
					.addChildNode(priorityTopicPriorProb, 
							"human_rights", "Human Rights")
					.addPriorityWords(
							"human rights violation torture campaign campaigners activists unpunished " +
							"commission commissioner responsible procecute amnesty international " +
							"conditions prison prisoners abuse")
					.isGroup(true);
			
				issuesNode
					.addChildNode(priorityTopicPriorProb, 
							"religion", "Religion")
					.addPriorityWords(
							"pope benedict francis church priest bishop evangelical protestant " +
							"jesus christ sect religion religious catholic catholicism " +
							"buddhist monk monastery clergy ceremony crucifixion " +
							"pointiff christian gospel vatican cardinal " +
							"denominations parish chapel kaballah judaism " +
							"chabad synangogue moscue muslim islam")
					.isGroup(true);
				
				issuesNode
					.addChildNode(priorityTopicPriorProb, 
							"charity", "Charity")
					.addPriorityWords(
							"charity donation gift foundation personal help receive fortune former " +
							"charitable philanthropy nonprofit benefit")
					.isGroup(true);
				
				issuesNode
					.addChildNode(priorityTopicPriorProb, 
							"health", "Health")
					.addPriorityWords(
							"health care healthcare nhs obamacare program patients " +
							"insurance benefits retirement hospitals nurces doctors ")
					.isGroup(true);
				
				issuesNode
					.addChildNode(priorityTopicPriorProb, 
							"harassment", "Harassment")
					.addPriorityWords(
							"race racism racial rasist black latino asian " +
							"comments remarks violence discrimination " +
							"harassment sexual resign accused allegations " +
							"slurs apologised apology outrage " +
							"abuse bullying bullied ")
					.isGroup(true);
				
				issuesNode
					.addChildNode(priorityTopicPriorProb, 
							"gender", "Gender")
					.addPriorityWords(
							"women female boy girl " +
							"gay lesbian same-sex sex rape couples " +
							"bill ban violence discrimination equal rights ")
					.isGroup(true);
			}
		}
		
		{ // disasters ========>
			
			LDABuildTopicsTreeNode disastersNode 
				= rootNode
					.addChildNode(0, 
							"disasters", "Disasters")
					.addPriorityWords(
							"accident disaster incident inquest rescue " +
							"killed injured people victims catastrophe")
					.isGroup(true);
			
			{ // transportation -------->
				
				LDABuildTopicsTreeNode transportationNode 
					= disastersNode
						.addChildNode(0, 
								"transportation", "Transportation")
						.addPriorityWords(
								"passengers crash wreck transportation escaped ")
						.isGroup(true)
						.isGroupTaxonomy(true);
			
				transportationNode
					.addChildNode(priorityTopicPriorProb, 
							"air", "Air")
					.addPriorityWords(
							"airplane aircraft plane jet helicopter chopper aviation " +
							"grond attendants passengers airline flight pilot engine " +
							"fire airport landing takeoff investigators emergency ")
					.isGroup(true);
				
				transportationNode
					.addChildNode(priorityTopicPriorProb, 
							"road", "Road")
					.addPriorityWords(
							"car bus lorry truck van bike speed hit injured road drive " +
							"driver driven highway vehicles crossing red light " +
							"drunk lane route ")
					.isGroup(true);
				
				transportationNode
					.addChildNode(priorityTopicPriorProb, 
							"rail", "Rail")
					.addPriorityWords(
							"train rail railway track runaway station derailed " +
							"passengers driver high-speed ")
					.isGroup(true);
				
				transportationNode
					.addChildNode(priorityTopicPriorProb, 
							"water", "Water")
					.addPriorityWords(
							"shop boat sink sank carrier liner cruise ")
					.isGroup(true);
			}
			
			{ // technological -------->
				
				LDABuildTopicsTreeNode technologicalNode 
					= disastersNode
						.addChildNode(0, 
								"technological", "Technological")
						.addPriorityWords(
								"technological factory plant ")
						.isGroup(true)
						.isGroupTaxonomy(true);
			
				technologicalNode
					.addChildNode(priorityTopicPriorProb, 
							"structures", "Structures")
					.addPriorityWords(
							"fire explosion blast building roof collapse " +
							"trapped structure mining miner mine under " +
							"ground outage electricity ")
					.isGroup(true);
				
				technologicalNode
					.addChildNode(priorityTopicPriorProb,
							"pollution", "Pollution")
					.addPriorityWords(
							"pollution chemical toxic nuclear leak " +
							"environment spilled safety warned " +
							"contaminated ")
					.isGroup(true);
			}
			
			{ // natural -------->
				
				LDABuildTopicsTreeNode naturalNode 
					= disastersNode
						.addChildNode(0, 
								"natural", "Natural")
						.addPriorityWords(
								"natural evacuated")
						.isGroup(true)
						.isGroupTaxonomy(true);
			
				naturalNode
					.addChildNode(priorityTopicPriorProb, 
							"flood", "Flood")
					.addPriorityWords(
							"flood flash heavy rain river bounds " +
							"under deep water washed away")
					.addExcludedWords(
							"shooting")
					.isGroup(true);
				
				naturalNode
					.addChildNode(priorityTopicPriorProb, 
							"wildfire", "Wildfire")
					.addPriorityWords(
							"wildfire fire burned firefighters forest " +
							"acres wind dry houses homes")
					.isGroup(true);
				
				naturalNode
					.addChildNode(priorityTopicPriorProb, 
							"quake_tsunami", "Quake-Tsunami")
					.addPriorityWords(
							"earthquake quake magnitude rattle kilometers " +
							"tsunami wave coast region")
					.addExcludedWords(
							"survey pirates")
					.isGroup(true);
				
				naturalNode
					.addChildNode(priorityTopicPriorProb, 
							"atmospheric", "Atmospheric")
					.addPriorityWords(
							"hurricane typhoon storm pacific atlantic ocean tropical " +
							"depression system showers rain wind speed " +
							"path mph high low pressure")
					.isGroup(true);
			}
		}

		{ // entertainment ========>
			
			LDABuildTopicsTreeNode entertainmentNode 
				= rootNode
					.addChildNode(0, 
							"entertainment", "Entertainment")
					.addPriorityWords(
							"entertainment people star")
					.isGroup(true);
			
			{ // media -------->
				
				LDABuildTopicsTreeNode mediaNode 
					= entertainmentNode
						.addChildNode(0, 
								"media", "Media")
						.addPriorityWords(
								"media producer award ceremony")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"music", "Music")
					.addPriorityWords(
							"music video fans pop dance artists band rock " +
							"punk rap rapper concert stage record musician top " +
							"mtv hit sound single album song singer released " +
							"performance festival radio grammy billboard")
					.isGroup(true);
				
				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"film", "Film")
					.addPriorityWords(
							"film movie box office red carpet animated cartoon " +
							"premiere hollywood opening release top flop debut " +
							"director comedy drama sequel auditions action " +
							"actor actress theater oscar")
					.isGroup(true);
				
				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"tv", "TV")
					.addPriorityWords(
							"tv television show reality series season episode " +
							"broadcast channel newsroom cable watch video network " +
							"viewers launch pilot anchor presenter big brother " +
							"commercial content emmy")
					.isGroup(true);
				
				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"fashion", "Fashion")
					.addPriorityWords(
							"fashion week show designer model new collection season " +
							"winter summer autumn fall spring dress costume apparel " +
							"brand label catwalk runway glamorous stylish patterns " +
							"gown casual wear texture chic colour trend")
					.isGroup(true);
				
				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"art", "Art")
					.addPriorityWords(
							"art artist gallery exhibition sketches display painter " +
							"painting drawing portrait pastel sculpture " +
							"installation modern classic artwork opening")
					.isGroup(true);
				
				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"photo", "Photo")
					.addPriorityWords(
							"art artist gallery exhibition display released " +
							"photo photography opening pictures images camera " +
							"captured moment ")
					.isGroup(true);

				mediaNode
					.addChildNode(priorityTopicPriorProb, 
							"books", "Books")
					.addPriorityWords(
							"book writer review published publication pages reading " +
							"text manuscript literary editor titles series novel " +
							"story critic notes biography biographical")
					.addExcludedWords(
							"newspaper")
					.isGroup(true);
			}
			
			{ // celebrities -------->
				
				LDABuildTopicsTreeNode celebritiesNode 
					= entertainmentNode
						.addChildNode(0, 
								"celebrities", "Celebrities")
						.addPriorityWords(
								"celebrities")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				celebritiesNode
					.addChildNode(priorityTopicPriorProb, 
							"gossip", "Gossip")
					.addPriorityWords(
							"gossip rumors love affair boyfriend girlfriend seen husband wife " +
							"sighted weds wedding divorse marry actor actress musician family friends " +
							"vacation ceremony glamour wearing cover photo spans pararazzi " +
							"naked underwear magazine topless")
					.isGroup(true);
				
				celebritiesNode
					.addChildNode(priorityTopicPriorProb, 
							"royal", "Royal")
					.addPriorityWords(
							"royal family prince harry william baby king " +
							"queen duke duchess middleton")
					.isGroup(true);
			}
			
			{ // lifestyle -------->
				
				LDABuildTopicsTreeNode lifestyleNode 
					= entertainmentNode
						.addChildNode(0, 
								"lifestyle", "Lifestyle")
						.addPriorityWords(
								"lifestyle")
						.isGroup(true)
						.isGroupTaxonomy(true);
				
				lifestyleNode
					.addChildNode(priorityTopicPriorProb, 
							"events", "Events")
					.addPriorityWords(
							"city installation outdoor art museum park street open project " +
							"exhibition market town old view culture celebrate space green " +
							"modern site display area visitors tourists doors festival " +
							"bull gored race firework event annual parade")
					.addExcludedWords(
							"wall journal finance bank trader")
					.isGroup(true);
				
				lifestyleNode
					.addChildNode(priorityTopicPriorProb, 
							"holidays", "Holidays")
					.addPriorityWords(
							"holidays tourist tourism arrival visitors travel sea " +
							"resort vacation summer hot hotel cruise")
					.isGroup(true);
				
				lifestyleNode
					.addChildNode(priorityTopicPriorProb, 
							"weather", "Weather")
					.addPriorityWords(
							"weather hot cold summer spring winter autumn fall " +
							"expected record temperatures good bad wind forecast")
					.isGroup(true);
				
				lifestyleNode
					.addChildNode(priorityTopicPriorProb, 
							"food", "Food")
					.addPriorityWords(
							"food cooking recepe healthy vegetables fruits breakfast " +
							"burger meat egg bread juice salt pepper chilli")
					.isGroup(true);

				lifestyleNode
					.addChildNode(priorityTopicPriorProb, 
							"drinking", "Drinking")
					.addPriorityWords(
							"bar drinking beer party alcohol hotel resort club scene " +
							"pub night spirits cocktails bottles drunk wine vodka " +
							"punch dance booze brawl throwing cup problem outside")
					.isGroup(true);
			}
		}
		{
			rootNode
				.addChildNode(priorityTopicPriorProb, 
						"sports", "Sports")
				.addPriorityWords(
						"sport foodball cricket ashes basketball league win won winner " +
						"score goal points play championship world cup competition " +
						"striker midfielder wimbledon tennis champion victory " +
						"title ")
				.isGroup(true);
		}
		
		return buildTopicsTree;
		
	}

}
